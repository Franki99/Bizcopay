'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { api, User } from '@/lib/api'
import { isLoggedIn } from '@/lib/auth'
import Sidebar from './Sidebar'
import TopBar from './TopBar'

interface Props {
  children: React.ReactNode
}

export default function PageShell({ children }: Props) {
  const router = useRouter()
  const [user, setUser] = useState<User | null>(null)

  useEffect(() => {
    if (!isLoggedIn()) { router.push('/login'); return }
    api.getMe().then(setUser).catch(() => router.push('/login'))
  }, [router])

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar />
      <div className="flex-1 flex flex-col min-h-screen overflow-hidden">
        <TopBar user={user} />
        <main className="flex-1 p-8 overflow-auto">{children}</main>
      </div>
    </div>
  )
}
