import { z } from 'zod'
import { prisma } from '../../lib/prisma'
import { AppError } from '../../middleware/error.middleware'
import { getIO } from '../../socket'

export const createRequestSchema = z.object({
  amount: z.number().positive().max(10_000_000),
  note: z.string().max(200).optional(),
})

export async function createRequest(userId: string, data: z.infer<typeof createRequestSchema>) {
  const wallet = await prisma.wallet.findUnique({ where: { userId } })
  if (!wallet) throw new AppError(404, 'Wallet not found')
  return prisma.topUpRequest.create({
    data: { userId, amount: data.amount, note: data.note },
    include: { user: { select: { id: true, name: true, email: true } } },
  })
}

export async function getAllRequests() {
  return prisma.topUpRequest.findMany({
    orderBy: { createdAt: 'desc' },
    include: { user: { select: { id: true, name: true, email: true } } },
  })
}

export async function getMyRequests(userId: string) {
  return prisma.topUpRequest.findMany({
    where: { userId },
    orderBy: { createdAt: 'desc' },
  })
}

export async function approveRequest(requestId: string) {
  const req = await prisma.topUpRequest.findUnique({ where: { id: requestId } })
  if (!req) throw new AppError(404, 'Request not found')
  if (req.status !== 'PENDING') throw new AppError(400, 'Request is no longer pending')

  return prisma.$transaction(async tx => {
    const updated = await tx.topUpRequest.update({
      where: { id: requestId },
      data: { status: 'APPROVED' },
      include: { user: { select: { id: true, name: true, email: true } } },
    })
    const wallet = await tx.wallet.update({
      where: { userId: req.userId },
      data: { balance: { increment: req.amount } },
    })
    try {
      getIO().to(`user:${req.userId}`).emit('wallet:topped_up', {
        amount: req.amount.toString(),
        balance: wallet.balance.toString(),
      })
    } catch (_) {}
    return updated
  })
}

export async function rejectRequest(requestId: string) {
  const req = await prisma.topUpRequest.findUnique({ where: { id: requestId } })
  if (!req) throw new AppError(404, 'Request not found')
  if (req.status !== 'PENDING') throw new AppError(400, 'Request is no longer pending')

  return prisma.topUpRequest.update({
    where: { id: requestId },
    data: { status: 'REJECTED' },
    include: { user: { select: { id: true, name: true, email: true } } },
  })
}
