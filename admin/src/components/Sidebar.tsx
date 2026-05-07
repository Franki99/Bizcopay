'use client'

import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { clearToken } from '@/lib/auth'

const links = [
  { href: '/',             label: 'Dashboard',    icon: '▦' },
  { href: '/users',        label: 'Users',        icon: '👥' },
  { href: '/transactions', label: 'Transactions', icon: '💳' },
  { href: '/fraud',        label: 'Fraud Alerts', icon: '🛡️' },
]

export default function Sidebar() {
  const pathname = usePathname()
  const router = useRouter()

  function logout() {
    clearToken()
    router.push('/login')
  }

  return (
    <aside className="w-56 min-h-screen bg-brand-700 text-white flex flex-col">
      <div className="px-6 py-5 border-b border-brand-600">
        <h1 className="text-xl font-bold tracking-tight">Bizcopay</h1>
        <p className="text-xs text-indigo-300 mt-0.5">Admin Panel</p>
      </div>

      <nav className="flex-1 px-3 py-4 space-y-1">
        {links.map(({ href, label, icon }) => {
          const active = pathname === href
          return (
            <Link
              key={href}
              href={href}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                active
                  ? 'bg-white/20 text-white'
                  : 'text-indigo-200 hover:bg-white/10 hover:text-white'
              }`}
            >
              <span>{icon}</span>
              {label}
            </Link>
          )
        })}
      </nav>

      <div className="px-3 py-4 border-t border-brand-600">
        <button
          onClick={logout}
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-indigo-200 hover:bg-white/10 hover:text-white transition-colors"
        >
          <span>⏏</span> Logout
        </button>
      </div>
    </aside>
  )
}
