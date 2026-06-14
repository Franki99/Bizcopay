'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { api, TopUpRequest } from '@/lib/api'
import PageShell from '@/components/PageShell'
import Pagination from '@/components/Pagination'

type Filter = 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED'

const statusColor: Record<string, string> = {
  PENDING:  'bg-yellow-100 text-yellow-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
}
const statusDot: Record<string, string> = {
  PENDING:  'bg-yellow-500',
  APPROVED: 'bg-green-500',
  REJECTED: 'bg-red-500',
}

const PAGE_SIZE = 8

export default function TopUpRequestsPage() {
  const router = useRouter()
  const [requests,  setRequests]  = useState<TopUpRequest[]>([])
  const [loading,   setLoading]   = useState(true)
  const [filter,    setFilter]    = useState<Filter>('ALL')
  const [search,    setSearch]    = useState('')
  const [page,      setPage]      = useState(1)
  const [acting,    setActing]    = useState<string | null>(null)

  async function load() {
    try { setRequests(await api.getTopUpRequests()) }
    catch { router.push('/login') }
    finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  async function approve(id: string) {
    setActing(id)
    try {
      const updated = await api.approveTopUp(id)
      setRequests(prev => prev.map(r => r.id === id ? { ...r, ...updated } : r))
    } catch (e: any) { alert(e.message) }
    finally { setActing(null) }
  }

  async function reject(id: string) {
    if (!confirm('Reject this top-up request?')) return
    setActing(id)
    try {
      const updated = await api.rejectTopUp(id)
      setRequests(prev => prev.map(r => r.id === id ? { ...r, ...updated } : r))
    } catch (e: any) { alert(e.message) }
    finally { setActing(null) }
  }

  function applyFilter(fn: () => void) { fn(); setPage(1) }

  const pending  = requests.filter(r => r.status === 'PENDING')
  const approved = requests.filter(r => r.status === 'APPROVED')
  const totalCredited = approved.reduce((s, r) => s + parseFloat(r.amount), 0)

  const filtered = requests.filter(r => {
    if (filter !== 'ALL' && r.status !== filter) return false
    if (search) {
      const q = search.toLowerCase()
      if (!r.user.name.toLowerCase().includes(q) &&
          !r.user.email.toLowerCase().includes(q) &&
          !r.amount.includes(q)) return false
    }
    return true
  })

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const safePage   = Math.min(page, totalPages)
  const paginated  = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE)

  const stats = [
    {
      label: 'Total Requests', value: requests.length,
      bg: 'bg-blue-50', iconColor: 'text-blue-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" /></svg>,
    },
    {
      label: 'Pending', value: pending.length,
      bg: 'bg-yellow-50', iconColor: 'text-yellow-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>,
    },
    {
      label: 'Approved', value: approved.length,
      bg: 'bg-green-50', iconColor: 'text-green-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>,
    },
    {
      label: 'Total Credited', value: `RWF ${totalCredited.toLocaleString('en-US', { minimumFractionDigits: 2 })}`,
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
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="text-2xl font-bold text-gray-900">Top-Up Requests</h2>
              <p className="text-sm text-gray-500 mt-1">
                {pending.length > 0
                  ? <span className="text-yellow-600 font-semibold">{pending.length} pending</span>
                  : 'No pending requests'
                }
                {' '}· {requests.length} total
              </p>
            </div>
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
            <div className="relative flex-1 min-w-52">
              <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input
                value={search} onChange={e => applyFilter(() => setSearch(e.target.value))}
                placeholder="Search by payer name, email, or amount…"
                className="w-full border border-gray-200 rounded-xl pl-9 pr-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
              />
            </div>
            <div className="flex gap-1.5">
              {(['ALL', 'PENDING', 'APPROVED', 'REJECTED'] as Filter[]).map(f => (
                <button key={f} onClick={() => applyFilter(() => setFilter(f))}
                  className={`px-3 py-2 rounded-xl text-xs font-semibold transition-colors ${
                    filter === f
                      ? f === 'PENDING'  ? 'bg-yellow-500 text-white shadow-sm'
                        : f === 'APPROVED' ? 'bg-green-600 text-white shadow-sm'
                        : f === 'REJECTED' ? 'bg-red-600 text-white shadow-sm'
                        : 'bg-blue-600 text-white shadow-sm'
                      : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                  }`}>
                  {f === 'ALL' ? `All (${requests.length})` : f === 'PENDING' ? `Pending (${pending.length})` : f === 'APPROVED' ? `Approved (${approved.length})` : `Rejected (${requests.filter(r => r.status === 'REJECTED').length})`}
                </button>
              ))}
            </div>
          </div>

          {/* Table */}
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-400 text-xs uppercase">
                <tr>
                  {['Payer', 'Amount', 'Note', 'Status', 'Requested', 'Actions'].map(h => (
                    <th key={h} className="px-5 py-3 text-left font-medium tracking-wide">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {paginated.map((req, i) => (
                  <tr key={req.id}
                    className={`hover:bg-gray-50 transition-colors ${i !== 0 ? 'border-t border-gray-50' : ''} ${req.status === 'PENDING' ? 'bg-yellow-50/30' : ''}`}>

                    {/* Payer */}
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-2.5">
                        <div className="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center text-xs font-bold shrink-0">
                          {req.user.name[0].toUpperCase()}
                        </div>
                        <div className="min-w-0">
                          <p className="font-semibold text-gray-900 truncate">{req.user.name}</p>
                          <p className="text-xs text-gray-400 truncate">{req.user.email}</p>
                        </div>
                      </div>
                    </td>

                    {/* Amount */}
                    <td className="px-5 py-3.5">
                      <span className="font-bold text-gray-900 text-base">RWF {parseFloat(req.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
                    </td>

                    {/* Note */}
                    <td className="px-5 py-3.5 text-gray-400 text-xs max-w-48 truncate">
                      {req.note ?? <span className="text-gray-200">—</span>}
                    </td>

                    {/* Status */}
                    <td className="px-5 py-3.5">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${statusColor[req.status]}`}>
                        <span className={`w-1.5 h-1.5 rounded-full ${statusDot[req.status]}`} />
                        {req.status}
                      </span>
                    </td>

                    {/* Date */}
                    <td className="px-5 py-3.5 text-xs text-gray-400">
                      {new Date(req.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
                      <br />
                      <span className="text-gray-300">{new Date(req.createdAt).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}</span>
                    </td>

                    {/* Actions */}
                    <td className="px-5 py-3.5">
                      {req.status === 'PENDING' ? (
                        <div className="flex gap-2">
                          <button
                            onClick={() => approve(req.id)}
                            disabled={acting === req.id}
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs rounded-lg bg-green-50 text-green-700 font-semibold hover:bg-green-100 disabled:opacity-50 transition-colors">
                            {acting === req.id ? (
                              <div className="w-3 h-3 border-2 border-green-300 border-t-green-700 rounded-full animate-spin" />
                            ) : (
                              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                              </svg>
                            )}
                            Approve
                          </button>
                          <button
                            onClick={() => reject(req.id)}
                            disabled={acting === req.id}
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs rounded-lg bg-red-50 text-red-600 font-semibold hover:bg-red-100 disabled:opacity-50 transition-colors">
                            <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                            Reject
                          </button>
                        </div>
                      ) : (
                        <span className="text-xs text-gray-300">—</span>
                      )}
                    </td>
                  </tr>
                ))}

                {filtered.length === 0 && (
                  <tr>
                    <td colSpan={6} className="px-5 py-16 text-center">
                      <div className="flex flex-col items-center gap-2">
                        <svg className="w-10 h-10 text-gray-200" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                        </svg>
                        <p className="text-sm text-gray-400">No requests found</p>
                        {filter !== 'ALL' && (
                          <button onClick={() => applyFilter(() => setFilter('ALL'))}
                            className="text-xs text-blue-600 hover:text-blue-700 mt-1">Show all</button>
                        )}
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
            <Pagination page={safePage} totalPages={totalPages} totalItems={filtered.length} pageSize={PAGE_SIZE} onChange={setPage} />
          </div>
        </>
      )}
    </PageShell>
  )
}
