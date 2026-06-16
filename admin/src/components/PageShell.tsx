'use client'

import { useEffect, useRef, useState } from 'react'
import { useRouter } from 'next/navigation'
import { api, User } from '@/lib/api'
import { isLoggedIn, clearToken } from '@/lib/auth'
import Sidebar from './Sidebar'
import TopBar from './TopBar'

const SESSION_TIMEOUT_MS = 10 * 60 * 1000  // 10 minutes
const WARN_BEFORE_MS     = 30 * 1000        // warn 30 s before expiry
const CHECK_INTERVAL_MS  = 5 * 1000         // check every 5 s

interface Props {
  children: React.ReactNode
}

export default function PageShell({ children }: Props) {
  const router    = useRouter()
  const [user, setUser] = useState<User | null>(null)
  const [warnVisible, setWarnVisible] = useState(false)
  const lastActivity = useRef(Date.now())

  // ── Auth + profile ─────────────────────────────────────────────────────────
  useEffect(() => {
    if (!isLoggedIn()) { router.push('/login'); return }
    api.getMe().then(setUser).catch(() => router.push('/login'))
    const refresh = () => api.getMe().then(setUser).catch(() => {})
    window.addEventListener('user-updated', refresh)
    return () => window.removeEventListener('user-updated', refresh)
  }, [router])

  // ── Idle session timeout ───────────────────────────────────────────────────
  useEffect(() => {
    const touch = () => {
      lastActivity.current = Date.now()
      setWarnVisible(false)
    }

    const events = ['mousemove', 'mousedown', 'keydown', 'touchstart', 'scroll', 'click']
    events.forEach(e => window.addEventListener(e, touch, { passive: true }))

    const timer = setInterval(() => {
      const idle = Date.now() - lastActivity.current
      if (idle >= SESSION_TIMEOUT_MS) {
        clearToken()
        router.push('/login')
      } else if (idle >= SESSION_TIMEOUT_MS - WARN_BEFORE_MS) {
        setWarnVisible(true)
      }
    }, CHECK_INTERVAL_MS)

    return () => {
      events.forEach(e => window.removeEventListener(e, touch))
      clearInterval(timer)
    }
  }, [router])

  const secondsLeft = Math.max(
    0,
    Math.ceil((SESSION_TIMEOUT_MS - (Date.now() - lastActivity.current)) / 1000),
  )

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar />
      <div className="flex-1 flex flex-col min-h-screen overflow-hidden">
        <TopBar user={user} />

        {/* Session-expiry warning banner */}
        {warnVisible && (
          <div className="flex items-center justify-between px-5 py-2.5 bg-amber-50 border-b border-amber-200 text-sm text-amber-800">
            <span>
              ⚠ Your session will expire in about{' '}
              <strong>{secondsLeft}s</strong> due to inactivity.
            </span>
            <button
              onClick={() => { lastActivity.current = Date.now(); setWarnVisible(false) }}
              className="ml-4 px-3 py-1 bg-amber-600 text-white text-xs font-semibold rounded-lg hover:bg-amber-700 transition-colors"
            >
              Stay signed in
            </button>
          </div>
        )}

        <main className="flex-1 p-8 overflow-auto">{children}</main>
      </div>
    </div>
  )
}
