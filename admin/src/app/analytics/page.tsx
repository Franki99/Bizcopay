'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import dynamic from 'next/dynamic'
import { api, Transaction } from '@/lib/api'
import PageShell from '@/components/PageShell'
import type { ChartProps } from './_Charts'

const Charts = dynamic<ChartProps>(
  () => import('./_Charts').then(m => m.default),
  {
    ssr: false,
    loading: () => (
      <div className="mt-6 space-y-6">
        {[220, 200, 200].map((h, i) => (
          <div key={i} className="bg-white rounded-xl border border-gray-100 shadow-sm p-6" style={{ height: h + 56 }}>
            <div className="h-3 w-48 bg-gray-100 rounded mb-4" />
            <div className="bg-gray-50 rounded-lg animate-pulse" style={{ height: h }} />
          </div>
        ))}
      </div>
    ),
  }
)

type Preset = '1D' | '7D' | '30D' | '90D' | 'ALL' | 'CUSTOM'

const PRESETS: { label: string; value: Preset }[] = [
  { label: 'Today',   value: '1D'  },
  { label: '7 days',  value: '7D'  },
  { label: '30 days', value: '30D' },
  { label: '90 days', value: '90D' },
  { label: 'All time',value: 'ALL' },
  { label: 'Custom',  value: 'CUSTOM' },
]

function startOfDay(d: Date) {
  const c = new Date(d); c.setHours(0, 0, 0, 0); return c
}

function filterByPeriod(transactions: Transaction[], preset: Preset, from: string, to: string) {
  if (preset === 'ALL') return transactions

  let start: Date
  let end = new Date()

  if (preset === 'CUSTOM') {
    if (!from && !to) return transactions
    start = from ? startOfDay(new Date(from)) : new Date(0)
    end   = to   ? new Date(new Date(to).setHours(23, 59, 59, 999)) : new Date()
  } else {
    const days = preset === '1D' ? 1 : preset === '7D' ? 7 : preset === '30D' ? 30 : 90
    start = startOfDay(new Date(Date.now() - (days - 1) * 86_400_000))
  }

  return transactions.filter(t => {
    const d = new Date(t.createdAt)
    return d >= start && d <= end
  })
}

function exportCSV(transactions: Transaction[]) {
  const headers = ['Date', 'Merchant', 'Payer', 'Amount (RWF)', 'Currency', 'Status', 'Risk Score', 'Fraud Rule', 'Description']
  const rows = transactions.map(tx => [
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

  const csv = [headers, ...rows]
    .map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))
    .join('\n')

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url  = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `bizcopay-report-${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

export default function AnalyticsPage() {
  const router = useRouter()
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [loading,  setLoading]  = useState(true)
  const [preset,   setPreset]   = useState<Preset>('ALL')
  const [fromDate, setFromDate] = useState('')
  const [toDate,   setToDate]   = useState('')

  useEffect(() => {
    api.getTransactions()
      .then(setTransactions)
      .catch(() => router.push('/login'))
      .finally(() => setLoading(false))
  }, [router])

  const filtered    = filterByPeriod(transactions, preset, fromDate, toDate)
  const approved    = filtered.filter(t => t.status === 'APPROVED')
  const failed      = filtered.filter(t => t.status === 'FAILED')
  const fraudulent  = filtered.filter(t => t.fraudLog)
  const totalVolume = approved.reduce((s, t) => s + parseFloat(t.amount), 0)
  const approvalRate = filtered.length ? (approved.length / filtered.length) * 100 : 0
  const fraudRate    = filtered.length ? (fraudulent.length / filtered.length) * 100 : 0
  const avgTx        = approved.length ? totalVolume / approved.length : 0

  const kpis = [
    { label: 'Total Volume',    value: `RWF ${totalVolume.toFixed(2)}`, sub: `${approved.length} approved txns` },
    { label: 'Approval Rate',   value: `${approvalRate.toFixed(1)}%`,   sub: `${failed.length} failed` },
    { label: 'Avg Transaction', value: `RWF ${avgTx.toFixed(2)}`,       sub: 'approved only' },
    { label: 'Fraud Rate',      value: `${fraudRate.toFixed(1)}%`,      sub: `${fraudulent.length} flagged` },
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
              <h2 className="text-2xl font-bold text-gray-900">Analytics</h2>
              <p className="text-sm text-gray-500 mt-1">
                {filtered.length} transaction{filtered.length !== 1 ? 's' : ''} in selected period
              </p>
            </div>
            <button
              onClick={() => exportCSV(filtered)}
              disabled={filtered.length === 0}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
              Export CSV
            </button>
          </div>

          {/* Period filter */}
          <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4 mb-6">
            <div className="flex flex-wrap items-center gap-2">
              {PRESETS.map(p => (
                <button
                  key={p.value}
                  onClick={() => { setPreset(p.value); setFromDate(''); setToDate('') }}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                    preset === p.value
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                >
                  {p.label}
                </button>
              ))}

              {preset === 'CUSTOM' && (
                <div className="flex items-center gap-2 ml-2">
                  <input
                    type="date"
                    value={fromDate}
                    max={toDate || undefined}
                    onChange={e => setFromDate(e.target.value)}
                    className="border border-gray-300 rounded-lg px-3 py-1.5 text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                  <span className="text-gray-400 text-xs">to</span>
                  <input
                    type="date"
                    value={toDate}
                    min={fromDate || undefined}
                    max={new Date().toISOString().slice(0, 10)}
                    onChange={e => setToDate(e.target.value)}
                    className="border border-gray-300 rounded-lg px-3 py-1.5 text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              )}
            </div>
          </div>

          {/* KPIs */}
          <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
            {kpis.map(k => (
              <div key={k.label} className="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
                <p className="text-sm text-gray-500 font-medium">{k.label}</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">{k.value}</p>
                <p className="text-xs text-gray-400 mt-1">{k.sub}</p>
              </div>
            ))}
          </div>

          {filtered.length === 0 ? (
            <div className="mt-12 text-center">
              <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto">
                <svg className="w-8 h-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
              <p className="text-gray-500 mt-3 font-medium">No transactions in this period</p>
              <button onClick={() => setPreset('ALL')} className="mt-2 text-sm text-blue-600 hover:text-blue-700">
                View all time
              </button>
            </div>
          ) : (
            <Charts transactions={filtered} />
          )}
        </>
      )}
    </PageShell>
  )
}
