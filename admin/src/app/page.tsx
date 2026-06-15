'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { api, Transaction, User } from '@/lib/api'
import PageShell from '@/components/PageShell'

function RefreshButton({ refreshing, onClick }: { refreshing: boolean; onClick: () => void }) {
  return (
    <button onClick={onClick} disabled={refreshing}
      className="inline-flex items-center gap-1.5 px-3 py-2 text-sm text-gray-600 border border-gray-200 bg-white rounded-xl hover:bg-gray-50 disabled:opacity-50 transition-colors shadow-sm">
      <svg className={`w-4 h-4 ${refreshing ? 'animate-spin' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
      </svg>
      {refreshing ? 'Refreshing…' : 'Refresh'}
    </button>
  )
}

// ─── Mini sparkline (pure SVG, no library) ───────────────────────────────────
function Sparkline({ values, stroke }: { values: number[]; stroke: string }) {
  if (values.length < 2 || values.every(v => v === 0)) return <div className="w-24 h-10" />
  const max = Math.max(...values, 1)
  const pts = values
    .map((v, i) => `${(i / (values.length - 1)) * 92},${36 - (v / max) * 32}`)
    .join(' ')
  return (
    <svg viewBox="0 0 92 38" className="w-24 h-10" preserveAspectRatio="none">
      <polyline points={pts} fill="none" stroke={stroke} strokeWidth="2.5"
        strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

// ─── Trend badge ─────────────────────────────────────────────────────────────
function Trend({ current, previous }: { current: number; previous: number }) {
  if (previous === 0 && current === 0) return null
  const pct = previous === 0 ? 100 : Math.round(((current - previous) / previous) * 100)
  const up = pct >= 0
  return (
    <span className={`inline-flex items-center gap-0.5 text-xs font-semibold px-2 py-0.5 rounded-full ${
      up ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
    }`}>
      {up
        ? <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}><path strokeLinecap="round" strokeLinejoin="round" d="M5 15l7-7 7 7" /></svg>
        : <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}><path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" /></svg>
      }
      {Math.abs(pct)}%
    </span>
  )
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
function last7Days() {
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(); d.setDate(d.getDate() - (6 - i)); d.setHours(0, 0, 0, 0); return d
  })
}

function countByDay(items: { createdAt: string }[], days: Date[]) {
  return days.map(day => {
    const next = new Date(day); next.setDate(next.getDate() + 1)
    return items.filter(x => { const d = new Date(x.createdAt); return d >= day && d < next }).length
  })
}

function sumByDay(txs: Transaction[], days: Date[]) {
  return days.map(day => {
    const next = new Date(day); next.setDate(next.getDate() + 1)
    return txs
      .filter(t => t.status === 'APPROVED' && new Date(t.createdAt) >= day && new Date(t.createdAt) < next)
      .reduce((s, t) => s + parseFloat(t.amount), 0)
  })
}

function thisWeek<T extends { createdAt: string }>(items: T[]) {
  const cut = new Date(); cut.setDate(cut.getDate() - 7)
  return items.filter(x => new Date(x.createdAt) >= cut)
}
function lastWeek<T extends { createdAt: string }>(items: T[]) {
  const cutStart = new Date(); cutStart.setDate(cutStart.getDate() - 14)
  const cutEnd   = new Date(); cutEnd.setDate(cutEnd.getDate() - 7)
  return items.filter(x => { const d = new Date(x.createdAt); return d >= cutStart && d < cutEnd })
}

const statusColor: Record<string, string> = {
  APPROVED:     'bg-green-100 text-green-700',
  FAILED:       'bg-red-100 text-red-700',
  PENDING:      'bg-yellow-100 text-yellow-700',
  AWAITING_PIN: 'bg-blue-100 text-blue-700',
  EXPIRED:      'bg-gray-100 text-gray-500',
}

const statusBar: Record<string, string> = {
  APPROVED:     'bg-green-500',
  FAILED:       'bg-red-500',
  PENDING:      'bg-yellow-400',
  AWAITING_PIN: 'bg-blue-400',
  EXPIRED:      'bg-gray-300',
}

// ─── Page ────────────────────────────────────────────────────────────────────
export default function DashboardPage() {
  const router = useRouter()
  const [users,        setUsers]        = useState<User[]>([])
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [loading,      setLoading]      = useState(true)
  const [refreshing,   setRefreshing]   = useState(false)

  async function load(refresh = false) {
    if (refresh) setRefreshing(true)
    try {
      const [u, t] = await Promise.all([api.getUsers(), api.getTransactions()])
      setUsers(u); setTransactions(t)
    } catch { router.push('/login') }
    finally { setLoading(false); setRefreshing(false) }
  }
  useEffect(() => { load() }, [])

  const days     = last7Days()
  const approved = transactions.filter(t => t.status === 'APPROVED')
  const fraud    = transactions.filter(t => t.fraudLog)
  const volume   = approved.reduce((s, t) => s + parseFloat(t.amount), 0)

  const kpis = [
    {
      label: 'Total Users',
      value: users.length,
      sub: `${users.filter(u => u.role === 'PAYER').length} payers · ${users.filter(u => u.role === 'MERCHANT').length} merchants`,
      spark: countByDay(users, days),
      stroke: '#3b82f6',
      bg: 'bg-blue-50', iconColor: 'text-blue-600',
      thisW: thisWeek(users).length, lastW: lastWeek(users).length,
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
      ),
    },
    {
      label: 'Transactions',
      value: transactions.length,
      sub: `${approved.length} approved · ${transactions.filter(t => t.status === 'FAILED').length} failed`,
      spark: countByDay(transactions, days),
      stroke: '#10b981',
      bg: 'bg-emerald-50', iconColor: 'text-emerald-600',
      thisW: thisWeek(transactions).length, lastW: lastWeek(transactions).length,
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
        </svg>
      ),
    },
    {
      label: 'Volume Approved',
      value: `RWF ${volume.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
      sub: `avg RWF ${approved.length ? (volume / approved.length).toFixed(2) : '0.00'} per txn`,
      spark: sumByDay(transactions, days),
      stroke: '#8b5cf6',
      bg: 'bg-violet-50', iconColor: 'text-violet-600',
      thisW: thisWeek(approved).reduce((s, t) => s + parseFloat(t.amount), 0),
      lastW: lastWeek(approved).reduce((s, t) => s + parseFloat(t.amount), 0),
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
    },
    {
      label: 'Fraud Alerts',
      value: fraud.length,
      sub: `${transactions.length ? ((fraud.length / transactions.length) * 100).toFixed(1) : '0.0'}% fraud rate`,
      spark: countByDay(fraud, days),
      stroke: '#ef4444',
      bg: 'bg-red-50', iconColor: 'text-red-600',
      thisW: thisWeek(fraud).length, lastW: lastWeek(fraud).length,
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
        </svg>
      ),
    },
  ]

  const statusGroups = ['APPROVED', 'FAILED', 'PENDING', 'AWAITING_PIN', 'EXPIRED'].map(s => ({
    status: s,
    count: transactions.filter(t => t.status === s).length,
  }))

  return (
    <PageShell>
      {loading ? (
        <div className="flex h-64 items-center justify-center">
          <div className="w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      ) : (
        <>
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-2xl font-bold text-gray-900">Dashboard</h2>
              <p className="text-sm text-gray-500 mt-1">System overview · last 7 days sparklines</p>
            </div>
            <RefreshButton refreshing={refreshing} onClick={() => load(true)} />
          </div>

          {/* KPI cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
            {kpis.map(k => (
              <div key={k.label} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
                <div className="flex items-start justify-between">
                  <div className={`w-10 h-10 rounded-xl ${k.bg} ${k.iconColor} flex items-center justify-center shrink-0`}>
                    {k.icon}
                  </div>
                  <Trend current={k.thisW} previous={k.lastW} />
                </div>
                <p className="text-2xl font-bold text-gray-900 mt-3 leading-none">{k.value}</p>
                <p className="text-sm text-gray-500 font-medium mt-1">{k.label}</p>
                <div className="flex items-center justify-between mt-3">
                  <p className="text-xs text-gray-400">{k.sub}</p>
                  <Sparkline values={k.spark} stroke={k.stroke} />
                </div>
              </div>
            ))}
          </div>

          {/* Middle row */}
          <div className="mt-5 grid grid-cols-1 lg:grid-cols-5 gap-4">
            {/* Transaction status breakdown */}
            <div className="lg:col-span-3 bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
              <div className="flex items-center justify-between mb-5">
                <h3 className="text-base font-semibold text-gray-800">Transaction Breakdown</h3>
                <Link href="/transactions" className="text-xs text-blue-600 hover:text-blue-700 font-medium">View all</Link>
              </div>
              <div className="space-y-3">
                {statusGroups.map(({ status, count }) => (
                  <div key={status}>
                    <div className="flex items-center justify-between mb-1">
                      <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${statusColor[status]}`}>{status.replace('_', ' ')}</span>
                      <span className="text-xs font-semibold text-gray-700">{count} <span className="text-gray-400 font-normal">({transactions.length ? ((count / transactions.length) * 100).toFixed(0) : 0}%)</span></span>
                    </div>
                    <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                      <div
                        className={`h-2 rounded-full transition-all duration-500 ${statusBar[status]}`}
                        style={{ width: transactions.length ? `${(count / transactions.length) * 100}%` : '0%' }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Recent fraud */}
            <div className="lg:col-span-2 bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
              <div className="flex items-center justify-between mb-5">
                <h3 className="text-base font-semibold text-gray-800">Fraud Alerts</h3>
                <Link href="/fraud" className="text-xs text-blue-600 hover:text-blue-700 font-medium">View all</Link>
              </div>
              {fraud.length === 0 ? (
                <div className="flex flex-col items-center justify-center h-32 text-center">
                  <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center mb-2">
                    <svg className="w-5 h-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                  <p className="text-sm text-gray-400">All clear</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {fraud.slice(0, 4).map(tx => (
                    <div key={tx.id} className="flex items-start gap-3">
                      <div className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 ${
                        tx.fraudLog!.riskScore === 'HIGH' ? 'bg-red-100' : 'bg-yellow-100'
                      }`}>
                        <svg className={`w-4 h-4 ${tx.fraudLog!.riskScore === 'HIGH' ? 'text-red-600' : 'text-yellow-600'}`}
                          fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                      </div>
                      <div className="min-w-0">
                        <p className="text-xs font-semibold text-gray-800 truncate">{tx.fraudLog!.ruleTriggered}</p>
                        <p className="text-xs text-gray-400 truncate">{tx.merchant.name} · RWF {tx.amount}</p>
                      </div>
                      <span className={`text-xs font-semibold shrink-0 ${tx.fraudLog!.riskScore === 'HIGH' ? 'text-red-600' : 'text-yellow-600'}`}>
                        {tx.fraudLog!.riskScore}
                      </span>
                    </div>
                  ))}
                  {fraud.length > 4 && (
                    <Link href="/fraud" className="block text-xs text-center text-blue-600 hover:text-blue-700 pt-1">
                      +{fraud.length - 4} more alerts
                    </Link>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* Recent transactions */}
          <div className="mt-5 bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-50">
              <h3 className="text-base font-semibold text-gray-800">Recent Transactions</h3>
              <Link href="/transactions" className="text-xs text-blue-600 hover:text-blue-700 font-medium">View all</Link>
            </div>
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-400 text-xs uppercase">
                <tr>
                  {['Merchant', 'Payer', 'Amount', 'Status', 'Risk', 'Date'].map(h => (
                    <th key={h} className="px-5 py-3 text-left font-medium tracking-wide">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {transactions.slice(0, 8).map((tx, i) => (
                  <tr key={tx.id} className={`hover:bg-gray-50 transition-colors ${i !== 0 ? 'border-t border-gray-50' : ''}`}>
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-2.5">
                        <div className="w-7 h-7 rounded-lg bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
                          {tx.merchant.name[0].toUpperCase()}
                        </div>
                        <span className="font-medium text-gray-800">{tx.merchant.name}</span>
                      </div>
                    </td>
                    <td className="px-5 py-3.5 text-gray-500">{tx.payer?.name ?? <span className="text-gray-300">—</span>}</td>
                    <td className="px-5 py-3.5 font-semibold text-gray-900">RWF {tx.amount}</td>
                    <td className="px-5 py-3.5">
                      <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${statusColor[tx.status] ?? 'bg-gray-100 text-gray-500'}`}>
                        {tx.status.replace('_', ' ')}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
                        tx.riskScore === 'HIGH'   ? 'bg-red-100 text-red-700' :
                        tx.riskScore === 'MEDIUM' ? 'bg-yellow-100 text-yellow-700' :
                        'bg-green-100 text-green-700'
                      }`}>
                        {tx.riskScore}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 text-gray-400 text-xs">{new Date(tx.createdAt).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' })}</td>
                  </tr>
                ))}
                {transactions.length === 0 && (
                  <tr><td colSpan={6} className="px-5 py-12 text-center text-gray-400">No transactions yet</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </PageShell>
  )
}
