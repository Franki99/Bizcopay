'use client'

import { useState } from 'react'
import { api, User } from '@/lib/api'

interface Props {
  user: User
  onClose: () => void
  onSuccess: () => void
}

export default function TopUpModal({ user, onClose, onSuccess }: Props) {
  const [amount, setAmount] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function submit() {
    const amt = parseFloat(amount)
    if (!amt || amt <= 0) { setError('Enter a valid amount'); return }
    setLoading(true)
    try {
      await api.topUp(user.id, amt)
      onSuccess()
      onClose()
    } catch (e: any) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-sm p-6">
        <h2 className="text-lg font-semibold text-gray-900">Top Up Wallet</h2>
        <p className="text-sm text-gray-500 mt-1">
          Adding funds to <span className="font-medium">{user.name}</span>
        </p>
        <p className="text-xs text-gray-400 mt-0.5">
          Current balance: {user.wallet?.currency} {user.wallet?.balance ?? '—'}
        </p>

        <input
          type="number"
          value={amount}
          onChange={e => setAmount(e.target.value)}
          placeholder="Amount (USD)"
          className="mt-4 w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        />
        {error && <p className="text-xs text-red-500 mt-1">{error}</p>}

        <div className="flex gap-2 mt-4">
          <button onClick={onClose}
            className="flex-1 px-4 py-2 rounded-lg border border-gray-300 text-sm text-gray-700 hover:bg-gray-50">
            Cancel
          </button>
          <button onClick={submit} disabled={loading}
            className="flex-1 px-4 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700 disabled:opacity-50">
            {loading ? 'Processing...' : 'Top Up'}
          </button>
        </div>
      </div>
    </div>
  )
}
