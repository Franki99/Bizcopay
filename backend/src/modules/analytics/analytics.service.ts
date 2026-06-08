import { TransactionStatus } from '@prisma/client'
import { prisma } from '../../lib/prisma'

type Tx = { amount: any; createdAt: Date }
type ChartPoint = { label: string; amount: number; count: number }

function buildChartPoints(txs: Tx[], period: 'week' | 'month' | 'year'): ChartPoint[] {
  const now = new Date()

  if (period === 'week') {
    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(now)
      d.setDate(d.getDate() - (6 - i))
      d.setHours(0, 0, 0, 0)
      const next = new Date(d)
      next.setDate(next.getDate() + 1)
      const day = txs.filter(tx => tx.createdAt >= d && tx.createdAt < next)
      return {
        label: d.toLocaleDateString('en-US', { weekday: 'short' }),
        amount: day.reduce((s, tx) => s + Number(tx.amount), 0),
        count: day.length,
      }
    })
  }

  if (period === 'month') {
    const year = now.getFullYear()
    const month = now.getMonth()
    const today = now.getDate()
    return Array.from({ length: today }, (_, i) => {
      const d = new Date(year, month, i + 1)
      const next = new Date(year, month, i + 2)
      const day = txs.filter(tx => tx.createdAt >= d && tx.createdAt < next)
      return {
        label: String(i + 1),
        amount: day.reduce((s, tx) => s + Number(tx.amount), 0),
        count: day.length,
      }
    })
  }

  // year — one point per month up to the current month
  const year = now.getFullYear()
  const monthLabels = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']
  return Array.from({ length: now.getMonth() + 1 }, (_, m) => {
    const start = new Date(year, m, 1)
    const end   = new Date(year, m + 1, 1)
    const mo = txs.filter(tx => tx.createdAt >= start && tx.createdAt < end)
    return {
      label: monthLabels[m],
      amount: mo.reduce((s, tx) => s + Number(tx.amount), 0),
      count: mo.length,
    }
  })
}

export async function getPayerAnalytics(payerId: string, period: 'week' | 'month' | 'year' = 'year') {
  const now = new Date()
  let since: Date
  if (period === 'week')       since = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
  else if (period === 'month') since = new Date(now.getFullYear(), now.getMonth(), 1)
  else                         since = new Date(now.getFullYear(), 0, 1)

  const txs = await prisma.transaction.findMany({
    where: { payerId, status: TransactionStatus.APPROVED, createdAt: { gte: since } },
    select: { amount: true, category: true, createdAt: true },
  })

  const totalSpent = txs.reduce((s, tx) => s + Number(tx.amount), 0)
  const som = new Date(now.getFullYear(), now.getMonth(), 1)
  const thisMonth = txs
    .filter(tx => tx.createdAt >= som)
    .reduce((s, tx) => s + Number(tx.amount), 0)

  const catMap: Record<string, { amount: number; count: number }> = {}
  txs.forEach(tx => {
    const c = tx.category ?? 'OTHER'
    if (!catMap[c]) catMap[c] = { amount: 0, count: 0 }
    catMap[c].amount += Number(tx.amount)
    catMap[c].count++
  })
  const byCategory = Object.entries(catMap)
    .map(([category, d]) => ({ category, ...d }))
    .sort((a, b) => b.amount - a.amount)

  const chartPoints = buildChartPoints(txs, period)
  return { totalSpent, thisMonth, byCategory, chartPoints }
}

export async function getMerchantAnalytics(merchantId: string, period: 'week' | 'month' | 'year' = 'year') {
  const now = new Date()
  let since: Date
  if (period === 'week')       since = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
  else if (period === 'month') since = new Date(now.getFullYear(), now.getMonth(), 1)
  else                         since = new Date(now.getFullYear(), 0, 1)

  const txs = await prisma.transaction.findMany({
    where: { merchantId, status: TransactionStatus.APPROVED, createdAt: { gte: since } },
    select: { amount: true, createdAt: true },
  })

  const totalRevenue = txs.reduce((s, tx) => s + Number(tx.amount), 0)
  const som = new Date(now.getFullYear(), now.getMonth(), 1)
  const thisMonth = txs
    .filter(tx => tx.createdAt >= som)
    .reduce((s, tx) => s + Number(tx.amount), 0)
  const chartPoints = buildChartPoints(txs, period)

  return { totalRevenue, thisMonth, transactionCount: txs.length, chartPoints }
}
