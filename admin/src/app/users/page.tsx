'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { api, User } from '@/lib/api'
import PageShell from '@/components/PageShell'
import TopUpModal from '@/components/TopUpModal'
import AssignNfcModal from '@/components/AssignNfcModal'
import Pagination from '@/components/Pagination'

const PAGE_SIZE = 6

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

const ROLE_FILTERS = ['ALL', 'PAYER', 'MERCHANT', 'ADMIN'] as const
type RoleFilter = typeof ROLE_FILTERS[number]

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
const roleIcon: Record<string, JSX.Element> = {
  PAYER: (
    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
    </svg>
  ),
  MERCHANT: (
    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
    </svg>
  ),
  ADMIN: (
    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
    </svg>
  ),
}

export default function UsersPage() {
  const router = useRouter()
  const [users,        setUsers]        = useState<User[]>([])
  const [loading,      setLoading]      = useState(true)
  const [refreshing,   setRefreshing]   = useState(false)
  const [topUpTarget,  setTopUpTarget]  = useState<User | null>(null)
  const [nfcTarget,    setNfcTarget]    = useState<User | null>(null)
  const [search,       setSearch]       = useState('')
  const [roleFilter,   setRoleFilter]   = useState<RoleFilter>('ALL')
  const [page,         setPage]         = useState(1)

  async function load(refresh = false) {
    if (refresh) setRefreshing(true)
    try { setUsers(await api.getUsers()) }
    catch { router.push('/login') }
    finally { setLoading(false); setRefreshing(false) }
  }
  useEffect(() => { load() }, [])

  async function deactivate(user: User) {
    if (!confirm(`Deactivate ${user.name}?`)) return
    await api.deactivateUser(user.id)
    load()
  }

  const filtered = users.filter(u => {
    const matchRole   = roleFilter === 'ALL' || u.role === roleFilter
    const matchSearch = u.name.toLowerCase().includes(search.toLowerCase()) ||
                        u.email.toLowerCase().includes(search.toLowerCase())
    return matchRole && matchSearch
  })

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const safePage   = Math.min(page, totalPages)
  const paginated  = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE)

  function applyFilter(fn: () => void) { fn(); setPage(1) }

  const payers    = users.filter(u => u.role === 'PAYER')
  const merchants = users.filter(u => u.role === 'MERCHANT')
  const activeAll = users.filter(u => u.isActive)

  const stats = [
    {
      label: 'Total Users', value: users.length,
      bg: 'bg-blue-50', iconColor: 'text-blue-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" /></svg>,
    },
    {
      label: 'Payers', value: payers.length,
      bg: 'bg-blue-50', iconColor: 'text-blue-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>,
    },
    {
      label: 'Merchants', value: merchants.length,
      bg: 'bg-violet-50', iconColor: 'text-violet-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" /></svg>,
    },
    {
      label: 'Active', value: activeAll.length,
      bg: 'bg-green-50', iconColor: 'text-green-600',
      icon: <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}><path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>,
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
              <h2 className="text-2xl font-bold text-gray-900">Users</h2>
              <p className="text-sm text-gray-500 mt-1">{users.length} registered accounts</p>
            </div>
            <RefreshButton refreshing={refreshing} onClick={() => load(true)} />
          </div>

          {/* Stat cards */}
          <div className="grid grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
            {stats.map(s => (
              <div key={s.label} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex items-center gap-3">
                <div className={`w-10 h-10 rounded-xl ${s.bg} ${s.iconColor} flex items-center justify-center shrink-0`}>
                  {s.icon}
                </div>
                <div>
                  <p className="text-xl font-bold text-gray-900 leading-none">{s.value}</p>
                  <p className="text-xs text-gray-500 mt-0.5">{s.label}</p>
                </div>
              </div>
            ))}
          </div>

          {/* Filters row */}
          <div className="flex flex-wrap items-center gap-3 mb-4">
            <div className="relative flex-1 min-w-52">
              <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input
                value={search} onChange={e => applyFilter(() => setSearch(e.target.value))}
                placeholder="Search by name or email…"
                className="w-full border border-gray-200 rounded-xl pl-9 pr-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
              />
            </div>
            <div className="flex gap-1.5">
              {ROLE_FILTERS.map(r => (
                <button key={r} onClick={() => applyFilter(() => setRoleFilter(r))}
                  className={`px-3 py-2 rounded-xl text-xs font-semibold transition-colors ${
                    roleFilter === r
                      ? 'bg-blue-600 text-white shadow-sm'
                      : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                  }`}>
                  {r === 'ALL' ? 'All roles' : r}
                </button>
              ))}
            </div>
          </div>

          {/* Table */}
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-400 text-xs uppercase">
                <tr>
                  {['User', 'Role', 'Balance', 'Status', 'Joined', 'Actions'].map(h => (
                    <th key={h} className="px-5 py-3 text-left font-medium tracking-wide">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {paginated.map((user, i) => (
                  <tr key={user.id} className={`hover:bg-gray-50 transition-colors ${i !== 0 ? 'border-t border-gray-50' : ''}`}>
                    {/* User avatar + name */}
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-3">
                        <div className={`w-9 h-9 rounded-full ${roleAvatar[user.role] ?? 'bg-gray-400'} flex items-center justify-center text-white text-sm font-bold shrink-0`}>
                          {user.name[0].toUpperCase()}
                        </div>
                        <div className="min-w-0">
                          <Link href={`/users/${user.id}`}
                            className="font-semibold text-gray-900 hover:text-blue-600 hover:underline truncate block transition-colors">
                            {user.name}
                          </Link>
                          <p className="text-xs text-gray-400 truncate">{user.email}</p>
                        </div>
                      </div>
                    </td>

                    {/* Role */}
                    <td className="px-5 py-3.5">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${roleBadge[user.role]}`}>
                        {roleIcon[user.role]}
                        {user.role}
                      </span>
                    </td>

                    {/* Balance */}
                    <td className="px-5 py-3.5">
                      {user.wallet
                        ? <span className="font-semibold text-gray-900">RWF {parseFloat(user.wallet.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
                        : <span className="text-gray-300">—</span>
                      }
                    </td>

                    {/* Status */}
                    <td className="px-5 py-3.5">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${user.isActive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                        <span className={`w-1.5 h-1.5 rounded-full ${user.isActive ? 'bg-green-500' : 'bg-red-500'}`} />
                        {user.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>

                    {/* Joined */}
                    <td className="px-5 py-3.5 text-xs text-gray-400">
                      {new Date(user.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
                    </td>

                    {/* Actions */}
                    <td className="px-5 py-3.5">
                      <div className="flex gap-2">
                        {user.role !== 'ADMIN' && (
                          <button onClick={() => setTopUpTarget(user)}
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs rounded-lg bg-blue-50 text-blue-700 font-semibold hover:bg-blue-100 transition-colors">
                            <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}><path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" /></svg>
                            Top Up
                          </button>
                        )}
                        {user.role === 'PAYER' && (
                          <button onClick={() => setNfcTarget(user)}
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs rounded-lg bg-gray-100 text-gray-700 font-semibold hover:bg-gray-200 transition-colors">
                            <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M8.111 16.404a5.5 5.5 0 017.778 0M12 20h.01m-7.08-7.071c3.904-3.905 10.236-3.905 14.141 0M1.394 9.393c5.857-5.857 15.355-5.857 21.213 0" /></svg>
                            NFC
                          </button>
                        )}
                        {user.isActive && user.role !== 'ADMIN' && (
                          <button onClick={() => deactivate(user)}
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs rounded-lg bg-red-50 text-red-600 font-semibold hover:bg-red-100 transition-colors">
                            <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}><path strokeLinecap="round" strokeLinejoin="round" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" /></svg>
                            Deactivate
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
                {filtered.length === 0 && (
                  <tr>
                    <td colSpan={6} className="px-5 py-16 text-center">
                      <div className="flex flex-col items-center gap-2">
                        <svg className="w-10 h-10 text-gray-200" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
                        </svg>
                        <p className="text-sm text-gray-400">No users match your search</p>
                        <button onClick={() => applyFilter(() => { setSearch(''); setRoleFilter('ALL') })}
                          className="text-xs text-blue-600 hover:text-blue-700 mt-1">
                          Clear filters
                        </button>
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
            <Pagination
              page={safePage}
              totalPages={totalPages}
              totalItems={filtered.length}
              pageSize={PAGE_SIZE}
              onChange={setPage}
            />
          </div>
        </>
      )}

      {topUpTarget && <TopUpModal user={topUpTarget} onClose={() => setTopUpTarget(null)} onSuccess={load} />}
      {nfcTarget   && <AssignNfcModal user={nfcTarget} onClose={() => setNfcTarget(null)} />}
    </PageShell>
  )
}
