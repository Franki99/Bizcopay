'use client'

import { useEffect, useState } from 'react'
import { useRouter, useParams } from 'next/navigation'
import Link from 'next/link'
import { api, UserDetail, Transaction, TopUpRequest } from '@/lib/api'
import PageShell from '@/components/PageShell'

// ─── Status / role colours ───────────────────────────────────────────────────
const roleBadge: Record<string, string> = {
  PAYER:    'bg-blue-100 text-blue-700',
  MERCHANT: 'bg-violet-100 text-violet-700',
  ADMIN:    'bg-orange-100 text-orange-700',
}
const roleAvatar: Record<string, string> = {
  PAYER:    'bg-blue-600',
  MERCHANT: 'bg-violet-600',
  ADMIN:    'bg-orange-500',
}
const txStatusColor: Record<string, string> = {
  APPROVED:     'bg-green-100 text-green-700',
  FAILED:       'bg-red-100 text-red-700',
  PENDING:      'bg-yellow-100 text-yellow-700',
  AWAITING_PIN: 'bg-blue-100 text-blue-700',
  EXPIRED:      'bg-gray-100 text-gray-500',
}
const txStatusDot: Record<string, string> = {
  APPROVED:     'bg-green-500',
  FAILED:       'bg-red-500',
  PENDING:      'bg-yellow-400',
  AWAITING_PIN: 'bg-blue-500',
  EXPIRED:      'bg-gray-400',
}
const topupColor: Record<string, string> = {
  PENDING:  'bg-yellow-100 text-yellow-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
}

// ─── Statement CSV export ─────────────────────────────────────────────────────
function downloadStatement(user: UserDetail, transactions: Transaction[], topUps: TopUpRequest[]) {
  const rows: string[][] = []

  rows.push(['Bizcopay Account Statement'])
  rows.push([`User: ${user.name}`, `Email: ${user.email}`, `Role: ${user.role}`])
  rows.push([`Generated: ${new Date().toLocaleString()}`])
  rows.push([])

  if (transactions.length > 0) {
    rows.push(['--- TRANSACTIONS ---'])
    rows.push(['Date', 'Type', 'Counterpart', 'Amount (RWF)', 'Status', 'Risk', 'Description'])
    for (const tx of transactions) {
      const isPayer = tx.payer?.id === user.id
      rows.push([
        new Date(tx.createdAt).toLocaleString(),
        isPayer ? 'Payment sent' : 'Payment received',
        isPayer ? (tx.merchant.name) : (tx.payer?.name ?? '—'),
        tx.amount,
        tx.status,
        tx.riskScore,
        tx.description ?? '',
      ])
    }
    rows.push([])
  }

  if (topUps.length > 0) {
    rows.push(['--- TOP-UP REQUESTS ---'])
    rows.push(['Date', 'Amount (RWF)', 'Note', 'Status'])
    for (const req of topUps) {
      rows.push([
        new Date(req.createdAt).toLocaleString(),
        req.amount,
        req.note ?? '',
        req.status,
      ])
    }
  }

  const csv = rows
    .map(r => r.map(c => `"${String(c).replace(/"/g, '""')}"`).join(','))
    .join('\n')

  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }))
  const a = document.createElement('a')
  a.href = url
  a.download = `statement-${user.name.replace(/\s+/g, '-').toLowerCase()}-${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

// ─── Page ─────────────────────────────────────────────────────────────────────
export default function UserDetailPage() {
  const router = useRouter()
  const { id } = useParams<{ id: string }>()

  const [user,         setUser]         = useState<UserDetail | null>(null)
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [topUps,       setTopUps]       = useState<TopUpRequest[]>([])
  const [loading,      setLoading]      = useState(true)
  const [txFilter,     setTxFilter]     = useState<'ALL' | 'APPROVED' | 'FAILED'>('ALL')

  useEffect(() => {
    if (!id) return
    Promise.all([
      api.getUserById(id),
      api.getUserTransactions(id),
      api.getUserTopUpRequests(id),
    ])
      .then(([u, txs, ups]) => { setUser(u); setTransactions(txs); setTopUps(ups) })
      .catch(() => router.push('/login'))
      .finally(() => setLoading(false))
  }, [id])

  const filteredTxs = transactions.filter(tx =>
    txFilter === 'ALL' || tx.status === txFilter
  )

  const approvedTxs = transactions.filter(t => t.status === 'APPROVED')
  const totalVolume = approvedTxs.reduce((s, t) => s + parseFloat(t.amount), 0)
  const totalTopUpCredited = topUps.filter(r => r.status === 'APPROVED').reduce((s, r) => s + parseFloat(r.amount), 0)

  if (loading) {
    return (
      <PageShell>
        <div className="flex h-64 items-center justify-center">
          <div className="w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      </PageShell>
    )
  }

  if (!user) {
    return (
      <PageShell>
        <div className="flex h-64 items-center justify-center text-gray-400">User not found</div>
      </PageShell>
    )
  }

  const isPayer    = user.role === 'PAYER'
  const isMerchant = user.role === 'MERCHANT'

  return (
    <PageShell>
      {/* Back + header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <Link href="/users"
            className="inline-flex items-center gap-1 px-3 py-2 text-sm text-gray-500 border border-gray-200 bg-white rounded-xl hover:bg-gray-50 transition-colors">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
            </svg>
            Users
          </Link>
          <span className="text-gray-300">/</span>
          <h2 className="text-xl font-bold text-gray-900">{user.name}</h2>
        </div>
        <button
          onClick={() => downloadStatement(user, transactions, topUps)}
          className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded-xl hover:bg-blue-700 transition-colors shadow-sm">
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
          </svg>
          Download Statement
        </button>
      </div>

      {/* Profile card */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 mb-6">
        <div className="flex flex-wrap items-start gap-5">
          {/* Avatar */}
          <div className={`w-16 h-16 rounded-2xl ${roleAvatar[user.role] ?? 'bg-gray-400'} flex items-center justify-center text-white text-2xl font-bold shrink-0`}>
            {user.name[0].toUpperCase()}
          </div>

          {/* Info */}
          <div className="flex-1 min-w-0">
            <div className="flex flex-wrap items-center gap-2 mb-1">
              <h3 className="text-xl font-bold text-gray-900">{user.name}</h3>
              <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${roleBadge[user.role] ?? 'bg-gray-100 text-gray-600'}`}>
                {user.role}
              </span>
              <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold ${user.isActive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                <span className={`w-1.5 h-1.5 rounded-full ${user.isActive ? 'bg-green-500' : 'bg-red-500'}`} />
                {user.isActive ? 'Active' : 'Inactive'}
              </span>
            </div>
            <p className="text-sm text-gray-500">{user.email}</p>
            <p className="text-xs text-gray-400 mt-1">
              Joined {new Date(user.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'long', year: 'numeric' })}
            </p>
          </div>

          {/* Stats */}
          <div className="flex flex-wrap gap-4">
            {user.wallet && (
              <div className="text-right">
                <p className="text-xs text-gray-400 uppercase tracking-wide">Wallet Balance</p>
                <p className="text-2xl font-bold text-gray-900">
                  {user.wallet.currency} {parseFloat(user.wallet.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </p>
              </div>
            )}
            <div className="text-right">
              <p className="text-xs text-gray-400 uppercase tracking-wide">Transactions</p>
              <p className="text-2xl font-bold text-gray-900">{transactions.length}</p>
            </div>
            {isPayer && (
              <>
                <div className="text-right">
                  <p className="text-xs text-gray-400 uppercase tracking-wide">Total Spent</p>
                  <p className="text-2xl font-bold text-gray-900">RWF {totalVolume.toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
                </div>
                {user.nfcTokens.length > 0 && (
                  <div className="text-right">
                    <p className="text-xs text-gray-400 uppercase tracking-wide">NFC Devices</p>
                    <p className="text-2xl font-bold text-gray-900">{user.nfcTokens.length}</p>
                  </div>
                )}
              </>
            )}
            {isMerchant && (
              <div className="text-right">
                <p className="text-xs text-gray-400 uppercase tracking-wide">Revenue Received</p>
                <p className="text-2xl font-bold text-gray-900">RWF {totalVolume.toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Transactions */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden mb-6">
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-50">
          <h4 className="font-semibold text-gray-900">
            {isPayer ? 'Payment History' : 'Transaction History'}
            <span className="ml-2 text-sm font-normal text-gray-400">({transactions.length})</span>
          </h4>
          <div className="flex gap-1.5">
            {(['ALL', 'APPROVED', 'FAILED'] as const).map(f => (
              <button key={f} onClick={() => setTxFilter(f)}
                className={`px-3 py-1.5 text-xs rounded-lg font-semibold transition-colors ${
                  txFilter === f
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}>
                {f === 'ALL' ? `All (${transactions.length})` : f === 'APPROVED' ? `Approved (${approvedTxs.length})` : `Failed (${transactions.filter(t => t.status === 'FAILED').length})`}
              </button>
            ))}
          </div>
        </div>

        {filteredTxs.length === 0 ? (
          <div className="py-16 text-center text-sm text-gray-400">No transactions found</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-400 text-xs uppercase">
              <tr>
                {(isPayer
                  ? ['Date', 'Merchant', 'Amount', 'Status', 'Risk']
                  : ['Date', 'Payer', 'Amount', 'Status', 'Risk']
                ).map(h => <th key={h} className="px-5 py-3 text-left font-medium tracking-wide">{h}</th>)}
              </tr>
            </thead>
            <tbody>
              {filteredTxs.map((tx, i) => (
                <tr key={tx.id} className={`hover:bg-gray-50 transition-colors ${i !== 0 ? 'border-t border-gray-50' : ''}`}>
                  <td className="px-5 py-3.5 text-xs text-gray-400">
                    {new Date(tx.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
                    <br /><span className="text-gray-300">{new Date(tx.createdAt).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}</span>
                  </td>
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-2">
                      <div className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold text-white shrink-0 ${isPayer ? 'bg-violet-500' : 'bg-blue-500'}`}>
                        {(isPayer ? tx.merchant.name : (tx.payer?.name ?? '?'))[0].toUpperCase()}
                      </div>
                      <div className="min-w-0">
                        <p className="font-medium text-gray-900 truncate">{isPayer ? tx.merchant.name : (tx.payer?.name ?? '—')}</p>
                        <p className="text-xs text-gray-400 truncate">{isPayer ? tx.merchant.email : (tx.payer?.email ?? '')}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-3.5">
                    <span className={`font-bold ${tx.status === 'APPROVED' ? (isPayer ? 'text-red-600' : 'text-green-600') : 'text-gray-400'}`}>
                      {isPayer ? '−' : '+'} RWF {parseFloat(tx.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </span>
                  </td>
                  <td className="px-5 py-3.5">
                    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${txStatusColor[tx.status] ?? 'bg-gray-100 text-gray-600'}`}>
                      <span className={`w-1.5 h-1.5 rounded-full ${txStatusDot[tx.status] ?? 'bg-gray-400'}`} />
                      {tx.status}
                    </span>
                  </td>
                  <td className="px-5 py-3.5">
                    {tx.fraudLog ? (
                      <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold ${tx.fraudLog.riskScore === 'HIGH' ? 'bg-red-100 text-red-700' : 'bg-yellow-100 text-yellow-700'}`}>
                        ⚠ {tx.fraudLog.riskScore}
                      </span>
                    ) : (
                      <span className="text-xs text-gray-300">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Top-Up Requests (payers only) */}
      {isPayer && (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-gray-50">
            <h4 className="font-semibold text-gray-900">
              Top-Up Requests
              <span className="ml-2 text-sm font-normal text-gray-400">({topUps.length})</span>
            </h4>
            {topUps.length > 0 && (
              <span className="text-xs text-gray-400">
                Total credited: <span className="font-semibold text-green-600">RWF {totalTopUpCredited.toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
              </span>
            )}
          </div>
          {topUps.length === 0 ? (
            <div className="py-16 text-center text-sm text-gray-400">No top-up requests</div>
          ) : (
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-400 text-xs uppercase">
                <tr>
                  {['Date', 'Amount', 'Note', 'Status'].map(h =>
                    <th key={h} className="px-5 py-3 text-left font-medium tracking-wide">{h}</th>
                  )}
                </tr>
              </thead>
              <tbody>
                {topUps.map((req, i) => (
                  <tr key={req.id} className={`hover:bg-gray-50 transition-colors ${i !== 0 ? 'border-t border-gray-50' : ''}`}>
                    <td className="px-5 py-3.5 text-xs text-gray-400">
                      {new Date(req.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
                      <br /><span className="text-gray-300">{new Date(req.createdAt).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}</span>
                    </td>
                    <td className="px-5 py-3.5 font-bold text-gray-900">
                      RWF {parseFloat(req.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="px-5 py-3.5 text-xs text-gray-400 max-w-48 truncate">
                      {req.note ?? <span className="text-gray-200">—</span>}
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${topupColor[req.status] ?? 'bg-gray-100 text-gray-600'}`}>
                        {req.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </PageShell>
  )
}
