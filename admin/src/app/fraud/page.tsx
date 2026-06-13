'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { api, Transaction } from '@/lib/api'
import PageShell from '@/components/PageShell'

type RiskFilter = 'ALL' | 'HIGH' | 'MEDIUM'

function RiskIcon({ level }: { level: string }) {
  return (
    <div className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 ${
      level === 'HIGH' ? 'bg-red-100' : 'bg-yellow-100'
    }`}>
      <svg className={`w-4 h-4 ${level === 'HIGH' ? 'text-red-600' : 'text-yellow-600'}`}
        fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round"
          d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
    </div>
  )
}

const statusColor: Record<string, string> = {
  APPROVED:     'bg-green-100 text-green-700',
  FAILED:       'bg-red-100 text-red-700',
  PENDING:      'bg-yellow-100 text-yellow-700',
  AWAITING_PIN: 'bg-blue-100 text-blue-700',
  EXPIRED:      'bg-gray-100 text-gray-500',
}

export default function FraudPage() {
  const router = useRouter()
  const [flagged, setFlagged] = useState<Transaction[]>([])
  const [loading, setLoading] = useState(true)
  const [riskFilter, setRiskFilter] = useState<RiskFilter>('ALL')

  useEffect(() => {
    api.getTransactions()
      .then(txs => setFlagged(txs.filter(t => t.fraudLog)))
      .catch(() => router.push('/login'))
      .finally(() => setLoading(false))
  }, [router])

  const high   = flagged.filter(t => t.fraudLog!.riskScore === 'HIGH')
  const medium = flagged.filter(t => t.fraudLog!.riskScore === 'MEDIUM')

  // Most triggered rule
  const ruleCounts = flagged.reduce<Record<string, number>>((acc, t) => {
    const r = t.fraudLog!.ruleTriggered
    acc[r] = (acc[r] ?? 0) + 1
    return acc
  }, {})
  const topRule = Object.entries(ruleCounts).sort((a, b) => b[1] - a[1])[0]

  const stats = [
    {
      label: 'Total Alerts', value: flagged.length,
      bg: 'bg-red-50', iconColor: 'text-red-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>,
    },
    {
      label: 'HIGH Risk', value: high.length,
      bg: 'bg-red-50', iconColor: 'text-red-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>,
    },
    {
      label: 'MEDIUM Risk', value: medium.length,
      bg: 'bg-yellow-50', iconColor: 'text-yellow-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>,
    },
    {
      label: 'Top Rule', value: topRule ? topRule[0].replace('_', ' ') : '—',
      sub: topRule ? `triggered ${topRule[1]}×` : '',
      bg: 'bg-orange-50', iconColor: 'text-orange-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" /></svg>,
    },
  ]

  const filtered = riskFilter === 'ALL' ? flagged : flagged.filter(t => t.fraudLog!.riskScore === riskFilter)

  return (
    <PageShell>
      {loading ? (
        <div className="flex h-64 items-center justify-center">
          <div className="w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      ) : (
        <>
          <div className="mb-6">
            <h2 className="text-2xl font-bold text-gray-900">Fraud Alerts</h2>
            <p className="text-sm text-gray-500 mt-1">
              {flagged.length} transaction{flagged.length !== 1 ? 's' : ''} flagged by the fraud engine
            </p>
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
                  {'sub' in s && s.sub && <p className="text-xs text-gray-400">{s.sub}</p>}
                </div>
              </div>
            ))}
          </div>

          {flagged.length === 0 ? (
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm py-20 text-center">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <p className="text-gray-700 font-semibold">All clear</p>
              <p className="text-sm text-gray-400 mt-1">No transactions have been flagged.</p>
            </div>
          ) : (
            <>
              {/* Risk filter */}
              <div className="flex gap-2 mb-4">
                {(['ALL', 'HIGH', 'MEDIUM'] as const).map(r => (
                  <button key={r} onClick={() => setRiskFilter(r)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-colors ${
                      riskFilter === r
                        ? r === 'HIGH'   ? 'bg-red-600 text-white'
                          : r === 'MEDIUM' ? 'bg-yellow-500 text-white'
                          : 'bg-blue-600 text-white'
                        : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                    }`}>
                    {r === 'ALL' ? `All (${flagged.length})` : r === 'HIGH' ? `HIGH (${high.length})` : `MEDIUM (${medium.length})`}
                  </button>
                ))}
              </div>

              {/* Table */}
              <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 text-gray-400 text-xs uppercase">
                    <tr>
                      {['Rule', 'Merchant', 'Payer', 'Amount', 'Risk', 'Tx Status', 'Date'].map(h => (
                        <th key={h} className="px-5 py-3 text-left font-medium tracking-wide">{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.map((tx, i) => (
                      <tr key={tx.id} className={`hover:bg-gray-50 transition-colors ${i !== 0 ? 'border-t border-gray-50' : ''}`}>
                        {/* Rule */}
                        <td className="px-5 py-3.5">
                          <div className="flex items-center gap-2.5">
                            <RiskIcon level={tx.fraudLog!.riskScore} />
                            <span className="font-medium text-gray-800 whitespace-nowrap">
                              {tx.fraudLog!.ruleTriggered.replace(/_/g, ' ')}
                            </span>
                          </div>
                        </td>

                        {/* Merchant */}
                        <td className="px-5 py-3.5">
                          <div className="flex items-center gap-2">
                            <div className="w-7 h-7 rounded-lg bg-violet-100 text-violet-700 flex items-center justify-center text-xs font-bold shrink-0">
                              {tx.merchant.name[0].toUpperCase()}
                            </div>
                            <span className="text-gray-700 font-medium">{tx.merchant.name}</span>
                          </div>
                        </td>

                        {/* Payer */}
                        <td className="px-5 py-3.5 text-gray-500">
                          {tx.payer?.name ?? <span className="text-gray-300">Unknown</span>}
                        </td>

                        {/* Amount */}
                        <td className="px-5 py-3.5 font-semibold text-gray-900">
                          RWF {tx.amount}
                        </td>

                        {/* Risk */}
                        <td className="px-5 py-3.5">
                          <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold ${
                            tx.fraudLog!.riskScore === 'HIGH'
                              ? 'bg-red-100 text-red-700'
                              : 'bg-yellow-100 text-yellow-700'
                          }`}>
                            <span className={`w-1.5 h-1.5 rounded-full ${tx.fraudLog!.riskScore === 'HIGH' ? 'bg-red-500' : 'bg-yellow-500'}`} />
                            {tx.fraudLog!.riskScore}
                          </span>
                        </td>

                        {/* Tx status */}
                        <td className="px-5 py-3.5">
                          <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${statusColor[tx.status] ?? 'bg-gray-100 text-gray-500'}`}>
                            {tx.status.replace('_', ' ')}
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
                        <td colSpan={7} className="px-5 py-12 text-center text-gray-400">
                          No {riskFilter} alerts
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </>
      )}
    </PageShell>
  )
}
