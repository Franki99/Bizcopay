'use client'

import { useEffect, useRef, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { clearToken } from '@/lib/auth'
import type { User } from '@/lib/api'

interface Props {
  user: User | null
}

function getGreeting() {
  const h = new Date().getHours()
  if (h < 12) return 'Good morning'
  if (h < 17) return 'Good afternoon'
  return 'Good evening'
}

function initials(name: string | undefined) {
  if (!name) return 'A'
  return name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()
}

export default function TopBar({ user }: Props) {
  const router = useRouter()
  const [open, setOpen] = useState(false)
  const [avatar, setAvatar] = useState<string | null>(null)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    setAvatar(localStorage.getItem('bizcopay_admin_avatar'))
    const handler = () => setAvatar(localStorage.getItem('bizcopay_admin_avatar'))
    window.addEventListener('avatar-updated', handler)
    return () => window.removeEventListener('avatar-updated', handler)
  }, [])

  useEffect(() => {
    function onClickOutside(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', onClickOutside)
    return () => document.removeEventListener('mousedown', onClickOutside)
  }, [])

  function logout() {
    clearToken()
    router.push('/login')
  }

  return (
    <header className="h-16 bg-white border-b border-gray-100 flex items-center justify-between px-8 sticky top-0 z-10 shadow-sm">
      <div>
        <p className="text-base font-semibold text-gray-900">
          {getGreeting()}, {user?.name?.split(' ')[0] ?? '…'}!
        </p>
        <p className="text-xs text-gray-400">Welcome to Bizcopay Admin</p>
      </div>

      <div className="relative" ref={ref}>
        <button
          onClick={() => setOpen(o => !o)}
          className={`flex items-center gap-3 rounded-xl border px-3 py-2 transition-colors ${
            open ? 'bg-gray-50 border-blue-200' : 'bg-white border-gray-200 hover:border-blue-200 hover:bg-gray-50'
          }`}
        >
          {avatar ? (
            <img src={avatar} alt="avatar" className="w-9 h-9 rounded-full object-cover ring-2 ring-blue-100 shrink-0" />
          ) : (
            <div className="w-9 h-9 rounded-full bg-blue-600 text-white flex items-center justify-center text-sm font-bold shrink-0">
              {initials(user?.name)}
            </div>
          )}
          <div className="text-left hidden sm:block">
            <p className="text-sm font-semibold text-gray-900 leading-tight">{user?.name ?? '…'}</p>
            <p className="text-xs text-gray-400 leading-tight capitalize">{user?.role?.toLowerCase() ?? ''}</p>
          </div>
          <svg className={`w-4 h-4 text-gray-400 transition-transform ${open ? 'rotate-180' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
          </svg>
        </button>

        {open && (
          <div className="absolute right-0 top-full mt-2 w-64 bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden z-20">
            <div className="px-4 py-4 bg-gradient-to-br from-blue-600 to-blue-700">
              <div className="flex items-center gap-3">
                {avatar ? (
                  <img src={avatar} alt="avatar" className="w-12 h-12 rounded-full object-cover ring-2 ring-white/30" />
                ) : (
                  <div className="w-12 h-12 rounded-full bg-white/20 text-white flex items-center justify-center text-base font-bold">
                    {initials(user?.name)}
                  </div>
                )}
                <div className="min-w-0">
                  <p className="text-sm font-semibold text-white truncate">{user?.name ?? '…'}</p>
                  <p className="text-xs text-blue-200 truncate">{user?.email ?? ''}</p>
                </div>
              </div>
            </div>

            <div className="py-1">
              <Link
                href="/profile"
                onClick={() => setOpen(false)}
                className="flex items-center gap-3 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
              >
                <svg className="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                My Profile
              </Link>

              <div className="border-t border-gray-100 mt-1 pt-1">
                <button
                  onClick={logout}
                  className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition-colors"
                >
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                  Sign out
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </header>
  )
}
