import { TransactionStatus } from '@prisma/client'
import { prisma } from '../../lib/prisma'

export async function getPayerAnalytics(payerId: string) {
  const txs = await prisma.transaction.findMany({
    where: { payerId, status: TransactionStatus.APPROVED },
    select: { amount: true, category: true, createdAt: true },
  })

  const totalSpent = txs.reduce((s, tx) => s + Number(tx.amount), 0)
  const now = new Date()
  const som = new Date(now.getFullYear(), now.getMonth(), 1)
  const thisMonth = txs.filter(tx => tx.createdAt >= som).reduce((s, tx) => s + Number(tx.amount), 0)

  const catMap: Record<string, { amount: number; count: number }> = {}
  txs.forEach(tx => {
    const c = tx.category ?? 'OTHER'
    if (!catMap[c]) catMap[c] = { amount: 0, count: 0 }
    catMap[c].amount += Number(tx.amount); catMap[c].count++
  })
  const byCategory = Object.entries(catMap)
    .map(([category, d]) => ({ category, ...d }))
    .sort((a, b) => b.amount - a.amount)

  const mMap: Record<string, number> = {}
  txs.forEach(tx => {
    const m = `${tx.createdAt.getFullYear()}-${String(tx.createdAt.getMonth()+1).padStart(2,'0')}`
    mMap[m] = (mMap[m] ?? 0) + Number(tx.amount)
  })
  const byMonth = Object.entries(mMap)
    .map(([month, amount]) => ({ month, amount }))
    .sort((a, b) => a.month.localeCompare(b.month)).slice(-6)

  return { totalSpent, thisMonth, byCategory, byMonth }
}

export async function getMerchantAnalytics(merchantId: string) {
  const txs = await prisma.transaction.findMany({
    where: { merchantId, status: TransactionStatus.APPROVED },
    select: { amount: true, createdAt: true },
  })

  const totalRevenue = txs.reduce((s, tx) => s + Number(tx.amount), 0)
  const now = new Date()
  const som = new Date(now.getFullYear(), now.getMonth(), 1)
  const thisMonth = txs.filter(tx => tx.createdAt >= som).reduce((s, tx) => s + Number(tx.amount), 0)

  const mMap: Record<string, { amount: number; count: number }> = {}
  txs.forEach(tx => {
    const m = `${tx.createdAt.getFullYear()}-${String(tx.createdAt.getMonth()+1).padStart(2,'0')}`
    if (!mMap[m]) mMap[m] = { amount: 0, count: 0 }
    mMap[m].amount += Number(tx.amount); mMap[m].count++
  })
  const byMonth = Object.entries(mMap)
    .map(([month, d]) => ({ month, ...d }))
    .sort((a, b) => a.month.localeCompare(b.month)).slice(-6)

  return { totalRevenue, thisMonth, transactionCount: txs.length, byMonth }
}
