'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { api, User } from '@/lib/api'
import { isLoggedIn } from '@/lib/auth'
import Sidebar from '@/components/Sidebar'
import TopUpModal from '@/components/TopUpModal'
import AssignNfcModal from '@/components/AssignNfcModal'

export default function UsersPage() {
  const router = useRouter()
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [topUpTarget, setTopUpTarget] = useState<User | null>(null)
  const [nfcTarget, setNfcTarget] = useState<User | null>(null)
  const [search, setSearch] = useState('')

  async function load() {
    try {
      setUsers(await api.getUsers())
    } catch {
      router.push('/login')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!isLoggedIn()) { router.push('/login'); return }
    load()
  }, [router])

  async function deactivate(user: User) {
    if (!confirm(`Deactivate ${user.name}?`)) return
    await api.deactivateUser(user.id)
    load()
  }

  const filtered = users.filter(u =>
    u.name.toLowerCase().includes(search.toLowerCase()) ||
    u.email.toLowerCase().includes(search.toLowerCase())
  )

  const roleBadge: Record<string, string> = {
    PAYER:    'bg-blue-100 text-blue-700',
    MERCHANT: 'bg-purple-100 text-purple-700',
    ADMIN:    'bg-orange-100 text-orange-700',
  }

  if (loading) return <div className="flex min-h-screen items-center justify-center"><p className="text-gray-400">Loading...</p></div>

  return (
    <div className="flex min-h-screen">
      <Sidebar />
      <main className="flex-1 p-8">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Users</h2>
            <p className="text-sm text-gray-500 mt-1">{users.length} registered accounts</p>
          </div>
          <input
            value={search} onChange={e => setSearch(e.target.value)}
            placeholder="Search by name or email..."
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm w-64 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        <div className="mt-6 bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
              <tr>
                {['Name', 'Email', 'Role', 'Balance', 'Status', 'Actions'].map(h => (
                  <th key={h} className="px-4 py-3 text-left font-medium">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {filtered.map(user => (
                <tr key={user.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">{user.name}</td>
                  <td className="px-4 py-3 text-gray-500">{user.email}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${roleBadge[user.role]}`}>
                      {user.role}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-semibold">
                    {user.wallet ? `${user.wallet.currency} ${user.wallet.balance}` : '—'}
                  </td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${user.isActive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                      {user.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 flex-wrap">
                      {user.role !== 'ADMIN' && (
                        <button onClick={() => setTopUpTarget(user)}
                          className="px-3 py-1 text-xs rounded-lg bg-brand-50 text-brand-600 font-medium hover:bg-brand-100 transition-colors">
                          Top Up
                        </button>
                      )}
                      {user.role === 'PAYER' && (
                        <button onClick={() => setNfcTarget(user)}
                          className="px-3 py-1 text-xs rounded-lg bg-gray-100 text-gray-700 font-medium hover:bg-gray-200 transition-colors">
                          NFC Tokens
                        </button>
                      )}
                      {user.isActive && user.role !== 'ADMIN' && (
                        <button onClick={() => deactivate(user)}
                          className="px-3 py-1 text-xs rounded-lg bg-red-50 text-red-600 font-medium hover:bg-red-100 transition-colors">
                          Deactivate
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan={6} className="px-4 py-8 text-center text-gray-400">No users found</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </main>

      {topUpTarget && (
        <TopUpModal
          user={topUpTarget}
          onClose={() => setTopUpTarget(null)}
          onSuccess={load}
        />
      )}

      {nfcTarget && (
        <AssignNfcModal
          user={nfcTarget}
          onClose={() => setNfcTarget(null)}
        />
      )}
    </div>
  )
}
