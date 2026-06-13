'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { api, Transaction, User } from '@/lib/api'
import PageShell from '@/components/PageShell'
import StatCard from '@/components/StatCard'

const statusColor: Record<string, string> = {
  APPROVED:     'bg-green-100 text-green-700',
  FAILED:       'bg-red-100 text-red-700',
  PENDING:      'bg-yellow-100 text-yellow-700',
  AWAITING_PIN: 'bg-blue-100 text-blue-700',
  EXPIRED:      'bg-gray-100 text-gray-600',
}

export default function DashboardPage() {
  const router = useRouter()
  const [users, setUsers] = useState<User[]>([])
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([api.getUsers(), api.getTransactions()])
      .then(([u, t]) => { setUsers(u); setTransactions(t) })
      .catch(() => router.push('/login'))
      .finally(() => setLoading(false))
  }, [router])

  const approved = transactions.filter(t => t.status === 'APPROVED')
  const fraudCount = transactions.filter(t => t.fraudLog).length
  const totalVolume = approved.reduce((sum, t) => sum + parseFloat(t.amount), 0)

  return (
    <PageShell>
      {loading ? (
        <div className="flex h-64 items-center justify-center">
          <div className="w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      ) : (
        <>
          <div className="mb-6">
            <h2 className="text-2xl font-bold text-gray-900">Dashboard</h2>
            <p className="text-sm text-gray-500 mt-1">System overview</p>
          </div>

          <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
            <StatCard label="Total Users"      value={users.length}                  icon="👥" />
            <StatCard label="Transactions"     value={transactions.length}            icon="💳" />
            <StatCard label="Volume Approved"  value={`RWF ${totalVolume.toFixed(2)}`}  icon="💰" />
            <StatCard label="Fraud Alerts"     value={fraudCount} icon="🛡️"
              sub={fraudCount > 0 ? 'Review required' : 'All clear'} />
          </div>

          <div className="mt-8">
            <h3 className="text-lg font-semibold text-gray-800 mb-3">Recent Transactions</h3>
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
                  <tr>
                    {['Merchant', 'Payer', 'Amount', 'Status', 'Risk', 'Date'].map(h => (
                      <th key={h} className="px-4 py-3 text-left font-medium">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {transactions.slice(0, 10).map(tx => (
                    <tr key={tx.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3 font-medium">{tx.merchant.name}</td>
                      <td className="px-4 py-3 text-gray-500">{tx.payer?.name ?? '—'}</td>
                      <td className="px-4 py-3 font-semibold">RWF {tx.amount}</td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusColor[tx.status] ?? 'bg-gray-100 text-gray-600'}`}>
                          {tx.status}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                          tx.riskScore === 'HIGH'   ? 'bg-red-100 text-red-700' :
                          tx.riskScore === 'MEDIUM' ? 'bg-yellow-100 text-yellow-700' :
                          'bg-green-100 text-green-700'}`}>
                          {tx.riskScore}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-gray-400">{new Date(tx.createdAt).toLocaleDateString()}</td>
                    </tr>
                  ))}
                  {transactions.length === 0 && (
                    <tr><td colSpan={6} className="px-4 py-8 text-center text-gray-400">No transactions yet</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </PageShell>
  )
}
