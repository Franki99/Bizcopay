'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { api, Transaction } from '@/lib/api'
import PageShell from '@/components/PageShell'

const STATUS_OPTIONS = ['ALL', 'APPROVED', 'FAILED', 'PENDING', 'AWAITING_PIN', 'EXPIRED']

const statusColor: Record<string, string> = {
  APPROVED:     'bg-green-100 text-green-700',
  FAILED:       'bg-red-100 text-red-700',
  PENDING:      'bg-yellow-100 text-yellow-700',
  AWAITING_PIN: 'bg-blue-100 text-blue-700',
  EXPIRED:      'bg-gray-100 text-gray-600',
}

const riskColor: Record<string, string> = {
  LOW:    'bg-green-100 text-green-700',
  MEDIUM: 'bg-yellow-100 text-yellow-700',
  HIGH:   'bg-red-100 text-red-700',
}

export default function TransactionsPage() {
  const router = useRouter()
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState('ALL')

  useEffect(() => {
    api.getTransactions()
      .then(setTransactions)
      .catch(() => router.push('/login'))
      .finally(() => setLoading(false))
  }, [router])

  const filtered = statusFilter === 'ALL'
    ? transactions
    : transactions.filter(t => t.status === statusFilter)

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
              <h2 className="text-2xl font-bold text-gray-900">Transactions</h2>
              <p className="text-sm text-gray-500 mt-1">{transactions.length} total</p>
            </div>
            <div className="flex gap-2 flex-wrap justify-end">
              {STATUS_OPTIONS.map(s => (
                <button key={s} onClick={() => setStatusFilter(s)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                    statusFilter === s
                      ? 'bg-blue-600 text-white'
                      : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                  }`}>
                  {s}
                </button>
              ))}
            </div>
          </div>

          <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
                <tr>
                  {['Merchant', 'Payer', 'Amount', 'Description', 'Status', 'Risk', 'Date'].map(h => (
                    <th key={h} className="px-4 py-3 text-left font-medium">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {filtered.map(tx => (
                  <tr key={tx.id} className={`hover:bg-gray-50 ${tx.fraudLog ? 'bg-red-50/30' : ''}`}>
                    <td className="px-4 py-3 font-medium">{tx.merchant.name}</td>
                    <td className="px-4 py-3 text-gray-500">{tx.payer?.name ?? '—'}</td>
                    <td className="px-4 py-3 font-semibold">RWF {tx.amount}</td>
                    <td className="px-4 py-3 text-gray-400">{tx.description ?? '—'}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusColor[tx.status] ?? 'bg-gray-100 text-gray-600'}`}>
                        {tx.status}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${riskColor[tx.riskScore]}`}>
                        {tx.riskScore}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-400 text-xs">{new Date(tx.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
                {filtered.length === 0 && (
                  <tr><td colSpan={7} className="px-4 py-8 text-center text-gray-400">No transactions found</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </PageShell>
  )
}
