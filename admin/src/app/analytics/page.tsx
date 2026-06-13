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

export default function AnalyticsPage() {
  const router = useRouter()
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.getTransactions()
      .then(setTransactions)
      .catch(() => router.push('/login'))
      .finally(() => setLoading(false))
  }, [router])

  const approved = transactions.filter(t => t.status === 'APPROVED')
  const failed = transactions.filter(t => t.status === 'FAILED')
  const fraudulent = transactions.filter(t => t.fraudLog)
  const totalVolume = approved.reduce((s, t) => s + parseFloat(t.amount), 0)
  const approvalRate = transactions.length ? (approved.length / transactions.length) * 100 : 0
  const fraudRate = transactions.length ? (fraudulent.length / transactions.length) * 100 : 0
  const avgTx = approved.length ? totalVolume / approved.length : 0

  const kpis = [
    { label: 'Total Volume',    value: `RWF ${totalVolume.toFixed(2)}`, sub: `${approved.length} approved txns` },
    { label: 'Approval Rate',   value: `${approvalRate.toFixed(1)}%`,  sub: `${failed.length} failed` },
    { label: 'Avg Transaction', value: `RWF ${avgTx.toFixed(2)}`,      sub: 'approved only' },
    { label: 'Fraud Rate',      value: `${fraudRate.toFixed(1)}%`,    sub: `${fraudulent.length} flagged` },
  ]

  return (
    <PageShell>
      {loading ? (
        <div className="flex h-64 items-center justify-center">
          <div className="w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      ) : (
        <>
          <div className="mb-6">
            <h2 className="text-2xl font-bold text-gray-900">Analytics</h2>
            <p className="text-sm text-gray-500 mt-1">Transaction insights across all time</p>
          </div>

          <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
            {kpis.map(k => (
              <div key={k.label} className="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
                <p className="text-sm text-gray-500 font-medium">{k.label}</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">{k.value}</p>
                <p className="text-xs text-gray-400 mt-1">{k.sub}</p>
              </div>
            ))}
          </div>

          <Charts transactions={transactions} />
        </>
      )}
    </PageShell>
  )
}
