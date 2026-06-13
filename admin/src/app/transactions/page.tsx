'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { api, Transaction } from '@/lib/api'
import PageShell from '@/components/PageShell'

const STATUS_OPTIONS = ['ALL', 'APPROVED', 'FAILED', 'PENDING', 'AWAITING_PIN', 'EXPIRED'] as const
type StatusFilter = typeof STATUS_OPTIONS[number]

const RISK_OPTIONS = ['ALL', 'LOW', 'MEDIUM', 'HIGH'] as const
type RiskFilter = typeof RISK_OPTIONS[number]

const statusColor: Record<string, string> = {
  APPROVED:     'bg-green-100 text-green-700',
  FAILED:       'bg-red-100 text-red-700',
  PENDING:      'bg-yellow-100 text-yellow-700',
  AWAITING_PIN: 'bg-blue-100 text-blue-700',
  EXPIRED:      'bg-gray-100 text-gray-500',
}
const statusDot: Record<string, string> = {
  APPROVED:     'bg-green-500',
  FAILED:       'bg-red-500',
  PENDING:      'bg-yellow-400',
  AWAITING_PIN: 'bg-blue-500',
  EXPIRED:      'bg-gray-400',
}
const riskColor: Record<string, string> = {
  LOW:    'bg-green-100 text-green-700',
  MEDIUM: 'bg-yellow-100 text-yellow-700',
  HIGH:   'bg-red-100 text-red-700',
}
const riskDot: Record<string, string> = {
  LOW:    'bg-green-500',
  MEDIUM: 'bg-yellow-500',
  HIGH:   'bg-red-500',
}

function exportCSV(rows: Transaction[]) {
  const headers = ['Date', 'Merchant', 'Payer', 'Amount (RWF)', 'Currency', 'Status', 'Risk', 'Fraud Rule', 'Description']
  const data = rows.map(tx => [
    new Date(tx.createdAt).toLocaleString(),
    tx.merchant.name,
    tx.payer?.name ?? '',
    tx.amount,
    tx.currency,
    tx.status,
    tx.riskScore,
    tx.fraudLog?.ruleTriggered ?? '',
    tx.description ?? '',
  ])
  const csv = [headers, ...data]
    .map(r => r.map(c => `"${String(c).replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }))
  const a = document.createElement('a')
  a.href = url
  a.download = `transactions-${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

export default function TransactionsPage() {
  const router = useRouter()
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [loading,      setLoading]      = useState(true)
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [riskFilter,   setRiskFilter]   = useState<RiskFilter>('ALL')
  const [search,       setSearch]       = useState('')

  useEffect(() => {
    api.getTransactions()
      .then(setTransactions)
      .catch(() => router.push('/login'))
      .finally(() => setLoading(false))
  }, [router])

  const filtered = transactions.filter(tx => {
    if (statusFilter !== 'ALL' && tx.status !== statusFilter) return false
    if (riskFilter   !== 'ALL' && tx.riskScore !== riskFilter) return false
    if (search) {
      const q = search.toLowerCase()
      if (!tx.merchant.name.toLowerCase().includes(q) &&
          !tx.payer?.name?.toLowerCase().includes(q) &&
          !tx.amount.includes(q)) return false
    }
    return true
  })

  const approved  = transactions.filter(t => t.status === 'APPROVED')
  const failed    = transactions.filter(t => t.status === 'FAILED')
  const volume    = approved.reduce((s, t) => s + parseFloat(t.amount), 0)
  const fraudFlag = transactions.filter(t => t.fraudLog)

  const stats = [
    {
      label: 'Total', value: transactions.length,
      bg: 'bg-blue-50', iconColor: 'text-blue-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" /></svg>,
    },
    {
      label: 'Approved', value: approved.length,
      bg: 'bg-green-50', iconColor: 'text-green-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>,
    },
    {
      label: 'Failed', value: failed.length,
      bg: 'bg-red-50', iconColor: 'text-red-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>,
    },
    {
      label: 'Volume', value: `RWF ${volume.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
      bg: 'bg-violet-50', iconColor: 'text-violet-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>,
    },
  ]

  return (
    <PageShell>
      {loading ? (
        <div className="flex h-64 items-center justify-center">
          <div className="w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      ) : (
        <>
          {/* Header */}
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-2xl font-bold text-gray-900">Transactions</h2>
              <p className="text-sm text-gray-500 mt-1">{transactions.length} total · {fraudFlag.length} flagged</p>
            </div>
            <button
              onClick={() => exportCSV(filtered)}
              disabled={filtered.length === 0}
              className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded-xl hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors shadow-sm"
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
              Export CSV
            </button>
          </div>

          {/* Stat cards */}
          <div className="grid grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
            {stats.map(s => (
              <div key={s.label} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex items-center gap-3">
                <div className={`w-10 h-10 rounded-xl ${s.bg} ${s.iconColor} flex items-center justify-center shrink-0`}>
                  {s.icon}
                </div>
                <div className="min-w-0">
                  <p className="text-xl font-bold text-gray-900 leading-none truncate">{s.value}</p>
                  <p className="text-xs text-gray-500 mt-0.5">{s.label}</p>
                </div>
              </div>
            ))}
          </div>

          {/* Filters */}
          <div className="flex flex-wrap items-center gap-3 mb-4">
            {/* Search */}
            <div className="relative flex-1 min-w-52">
              <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input
                value={search} onChange={e => setSearch(e.target.value)}
                placeholder="Search merchant, payer, or amount…"
                className="w-full border border-gray-200 rounded-xl pl-9 pr-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
              />
            </div>

            {/* Status filter */}
            <div className="flex gap-1.5 flex-wrap">
              {STATUS_OPTIONS.map(s => (
                <button key={s} onClick={() => setStatusFilter(s)}
                  className={`px-3 py-2 rounded-xl text-xs font-semibold transition-colors ${
                    statusFilter === s ? 'bg-blue-600 text-white shadow-sm' : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                  }`}>
                  {s === 'ALL' ? 'All statuses' : s.replace('_', ' ')}
                </button>
              ))}
            </div>

            {/* Risk filter */}
            <div className="flex gap-1.5">
              {RISK_OPTIONS.map(r => (
                <button key={r} onClick={() => setRiskFilter(r)}
                  className={`px-3 py-2 rounded-xl text-xs font-semibold transition-colors ${
                    riskFilter === r
                      ? r === 'HIGH'   ? 'bg-red-600 text-white shadow-sm'
                        : r === 'MEDIUM' ? 'bg-yellow-500 text-white shadow-sm'
                        : r === 'LOW'    ? 'bg-green-600 text-white shadow-sm'
                        : 'bg-blue-600 text-white shadow-sm'
                      : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                  }`}>
                  {r === 'ALL' ? 'All risk' : r}
                </button>
              ))}
            </div>
          </div>

          {/* Result count */}
          {(search || statusFilter !== 'ALL' || riskFilter !== 'ALL') && (
            <div className="flex items-center justify-between mb-3">
              <p className="text-xs text-gray-400">{filtered.length} result{filtered.length !== 1 ? 's' : ''}</p>
              <button onClick={() => { setSearch(''); setStatusFilter('ALL'); setRiskFilter('ALL') }}
                className="text-xs text-blue-600 hover:text-blue-700 font-medium">
                Clear filters
              </button>
            </div>
          )}

          {/* Table */}
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-400 text-xs uppercase">
                <tr>
                  {['Merchant', 'Payer', 'Amount', 'Description', 'Status', 'Risk', 'Date'].map(h => (
                    <th key={h} className="px-5 py-3 text-left font-medium tracking-wide">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filtered.map((tx, i) => (
                  <tr key={tx.id}
                    className={`hover:bg-gray-50 transition-colors ${i !== 0 ? 'border-t border-gray-50' : ''} ${tx.fraudLog ? 'bg-red-50/40' : ''}`}>

                    {/* Merchant */}
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-2.5">
                        <div className="w-8 h-8 rounded-lg bg-violet-100 text-violet-700 flex items-center justify-center text-xs font-bold shrink-0">
                          {tx.merchant.name[0].toUpperCase()}
                        </div>
                        <div className="min-w-0">
                          <p className="font-semibold text-gray-900 truncate">{tx.merchant.name}</p>
                          <p className="text-xs text-gray-400 truncate">{tx.merchant.email}</p>
                        </div>
                      </div>
                    </td>

                    {/* Payer */}
                    <td className="px-5 py-3.5">
                      {tx.payer ? (
                        <div className="flex items-center gap-2">
                          <div className="w-7 h-7 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center text-xs font-bold shrink-0">
                            {tx.payer.name[0].toUpperCase()}
                          </div>
                          <span className="text-gray-700 truncate">{tx.payer.name}</span>
                        </div>
                      ) : (
                        <span className="text-gray-300 text-xs">Pending NFC</span>
                      )}
                    </td>

                    {/* Amount */}
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-1.5">
                        {tx.fraudLog && (
                          <svg className="w-3.5 h-3.5 text-red-500 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                          </svg>
                        )}
                        <span className="font-semibold text-gray-900">RWF {tx.amount}</span>
                      </div>
                    </td>

                    {/* Description */}
                    <td className="px-5 py-3.5 text-gray-400 text-xs max-w-36 truncate">
                      {tx.description ?? <span className="text-gray-200">—</span>}
                    </td>

                    {/* Status */}
                    <td className="px-5 py-3.5">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${statusColor[tx.status] ?? 'bg-gray-100 text-gray-500'}`}>
                        <span className={`w-1.5 h-1.5 rounded-full ${statusDot[tx.status] ?? 'bg-gray-400'}`} />
                        {tx.status.replace('_', ' ')}
                      </span>
                    </td>

                    {/* Risk */}
                    <td className="px-5 py-3.5">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${riskColor[tx.riskScore]}`}>
                        <span className={`w-1.5 h-1.5 rounded-full ${riskDot[tx.riskScore]}`} />
                        {tx.riskScore}
                      </span>
                    </td>

                    {/* Date */}
                    <td className="px-5 py-3.5 text-xs text-gray-400">
                      {new Date(tx.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
                      <br />
                      <span className="text-gray-300">{new Date(tx.createdAt).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}</span>
                    </td>
                  </tr>
                ))}

                {filtered.length === 0 && (
                  <tr>
                    <td colSpan={7} className="px-5 py-16 text-center">
                      <div className="flex flex-col items-center gap-2">
                        <svg className="w-10 h-10 text-gray-200" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                        </svg>
                        <p className="text-sm text-gray-400">No transactions match your filters</p>
                        <button onClick={() => { setSearch(''); setStatusFilter('ALL'); setRiskFilter('ALL') }}
                          className="text-xs text-blue-600 hover:text-blue-700 mt-1">Clear filters</button>
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </PageShell>
  )
}
