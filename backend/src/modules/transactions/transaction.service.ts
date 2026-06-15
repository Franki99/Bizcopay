import { z } from 'zod'
import { Prisma, RiskScore, SpendingCategory, TransactionStatus } from '@prisma/client'
import { prisma } from '../../lib/prisma'
import { AppError } from '../../middleware/error.middleware'
import { getIO } from '../../socket'
import { evaluate } from '../fraud/fraud.service'
import { verifyPin } from '../auth/auth.service'

export const createTransactionSchema = z.object({
  amount: z.number().positive().max(5000000),
  description: z.string().max(200).optional(),
})

const TX_EXPIRY_MS = 5 * 60 * 1000

export async function create(merchantId: string, data: z.infer<typeof createTransactionSchema>) {
  return prisma.transaction.create({
    data: {
      amount: data.amount,
      description: data.description,
      merchantId,
      expiresAt: new Date(Date.now() + TX_EXPIRY_MS),
    },
  })
}

export async function resolveNfc(transactionId: string, nfcUid: string) {
  const transaction = await findOrThrow(transactionId)

  if (transaction.status !== TransactionStatus.PENDING) {
    throw new AppError(400, 'Transaction is not pending')
  }
  if (new Date() > transaction.expiresAt) {
    await prisma.transaction.update({
      where: { id: transactionId },
      data: { status: TransactionStatus.EXPIRED },
    })
    throw new AppError(400, 'Transaction has expired')
  }

  const nfcToken = await prisma.nfcToken.findUnique({
    where: { uid: nfcUid, isActive: true },
    include: { user: { include: { wallet: true } } },
  })
  if (!nfcToken) throw new AppError(404, 'NFC token not found or inactive')

  const payer = nfcToken.user
  const amount = Number(transaction.amount)
  const fraud = await evaluate(payer.id, amount)

  await prisma.transaction.update({
    where: { id: transactionId },
    data: { payerId: payer.id, nfcTokenUid: nfcUid, riskScore: fraud.riskScore },
  })

  if (fraud.ruleTriggered) {
    await prisma.fraudLog.create({
      data: {
        transactionId,
        ruleTriggered: fraud.ruleTriggered,
        riskScore: fraud.riskScore,
        details: fraud.details as Prisma.InputJsonValue,
      },
    })
  }

  const io = getIO()

  if (fraud.riskScore === RiskScore.HIGH) {
    await prisma.transaction.update({
      where: { id: transactionId },
      data: { status: TransactionStatus.FAILED, failureReason: `Fraud: ${fraud.ruleTriggered}` },
    })
    const failedPayload = { transactionId, reason: fraud.ruleTriggered }
    io.to(`transaction:${transactionId}`).emit('payment:failed', failedPayload)
    io.to(`user:${payer.id}`).emit('payment:failed', failedPayload)
    return { status: TransactionStatus.FAILED, riskScore: fraud.riskScore }
  }

  // MEDIUM and LOW risk both require PIN — always prompt the payer
  await prisma.transaction.update({
    where: { id: transactionId },
    data: { status: TransactionStatus.AWAITING_PIN },
  })
  const payload = { transactionId, amount, merchantName: transaction.merchant.name }
  io.to(`transaction:${transactionId}`).emit('payment:pending_pin', payload)
  io.to(`user:${payer.id}`).emit('payment:pending_pin', payload)
  return { status: TransactionStatus.AWAITING_PIN, riskScore: fraud.riskScore }
}

export async function approveWithPin(transactionId: string, pin: string) {
  const transaction = await findOrThrow(transactionId)

  if (transaction.status !== TransactionStatus.AWAITING_PIN) {
    throw new AppError(400, 'Transaction is not awaiting PIN')
  }
  if (!transaction.payerId) {
    throw new AppError(400, 'No payer linked to this transaction')
  }

  const pinValid = await verifyPin(transaction.payerId, pin)
  if (!pinValid) throw new AppError(401, 'Invalid PIN')

  const payer = await prisma.user.findUnique({ where: { id: transaction.payerId }, include: { wallet: true } })
  const amount = Number(transaction.amount)

  if (!payer?.wallet || Number(payer.wallet.balance) < amount) {
    await prisma.transaction.update({
      where: { id: transactionId },
      data: { status: TransactionStatus.FAILED, failureReason: 'Insufficient balance' },
    })
    const io = getIO()
    const failedPayload = { transactionId, reason: 'Insufficient balance' }
    io.to(`transaction:${transactionId}`).emit('payment:failed', failedPayload)
    io.to(`user:${transaction.payerId}`).emit('payment:failed', failedPayload)
    throw new AppError(400, 'Insufficient balance')
  }

  await processPayment(transactionId, transaction.payerId, transaction.merchantId, amount)
  return { status: TransactionStatus.APPROVED }
}

export async function getTransactions(userId: string, role: string) {
  const where =
    role === 'MERCHANT' ? { merchantId: userId } : role === 'ADMIN' ? {} : { payerId: userId }

  return prisma.transaction.findMany({
    where,
    include: {
      payer: { select: { id: true, name: true, email: true } },
      merchant: { select: { id: true, name: true, email: true } },
      fraudLog: true,
    },
    orderBy: { createdAt: 'desc' },
  })
}

export async function getTransactionsByUser(userId: string) {
  return prisma.transaction.findMany({
    where: { OR: [{ payerId: userId }, { merchantId: userId }] },
    include: {
      payer: { select: { id: true, name: true, email: true } },
      merchant: { select: { id: true, name: true, email: true } },
      fraudLog: true,
    },
    orderBy: { createdAt: 'desc' },
  })
}

async function processPayment(
  transactionId: string,
  payerId: string,
  merchantId: string,
  amount: number,
) {
  await prisma.$transaction([
    prisma.wallet.update({ where: { userId: payerId }, data: { balance: { decrement: amount } } }),
    prisma.wallet.update({ where: { userId: merchantId }, data: { balance: { increment: amount } } }),
    prisma.transaction.update({ where: { id: transactionId }, data: { status: TransactionStatus.APPROVED } }),
  ])

  const io = getIO()
  const payload = { transactionId, amount }
  io.to(`transaction:${transactionId}`).emit('payment:approved', payload)
  io.to(`user:${payerId}`).emit('payment:approved', payload)
  io.to(`user:${merchantId}`).emit('payment:received', payload)
}

export async function getMyTransactions(userId: string, role: string) {
  const where = role === 'MERCHANT'
    ? { merchantId: userId, status: { in: [TransactionStatus.APPROVED, TransactionStatus.FAILED] } }
    : { payerId: userId, status: { in: [TransactionStatus.APPROVED, TransactionStatus.FAILED] } }
  return prisma.transaction.findMany({
    where,
    include: {
      merchant: { select: { id: true, name: true } },
      payer:    { select: { id: true, name: true } },
    },
    orderBy: { createdAt: 'desc' },
    take: 100,
  })
}

export async function categorizeTransaction(transactionId: string, payerId: string, category: SpendingCategory) {
  const tx = await prisma.transaction.findUnique({ where: { id: transactionId } })
  if (!tx) throw new AppError(404, 'Transaction not found')
  if (tx.payerId !== payerId) throw new AppError(403, 'Not your transaction')
  return prisma.transaction.update({ where: { id: transactionId }, data: { category } })
}

export async function exportTransactionsCsv(userId: string, role: string): Promise<string> {
  const txs = await getMyTransactions(userId, role)
  const rows = txs.map(tx =>
    [tx.createdAt.toISOString(), Number(tx.amount).toFixed(2), 'RWF', tx.status,
     tx.merchant?.name ?? '', tx.description ?? '', tx.category ?? '']
      .map(v => `"${v}"`).join(',')
  )
  return ['Date,Amount,Currency,Status,Merchant,Description,Category', ...rows].join('\n')
}

async function findOrThrow(id: string) {
  const transaction = await prisma.transaction.findUnique({
    where: { id },
    include: { merchant: true },
  })
  if (!transaction) throw new AppError(404, 'Transaction not found')
  return transaction
}
