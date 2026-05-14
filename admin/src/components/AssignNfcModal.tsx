'use client'

import { useEffect, useState } from 'react'
import { api, NfcToken, User } from '@/lib/api'

interface Props {
  user: User
  onClose: () => void
}

export default function AssignNfcModal({ user, onClose }: Props) {
  const [tokens, setTokens] = useState<NfcToken[]>([])
  const [uid, setUid] = useState('')
  const [label, setLabel] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    api.getUserTokens(user.id).then(setTokens).catch(() => {})
  }, [user.id])

  async function assign() {
    if (!uid.trim()) return
    setLoading(true)
    setError('')
    setSuccess('')
    try {
      await api.assignNfcToken(user.id, uid.trim().toUpperCase(), label.trim())
      setSuccess(`Token assigned successfully to ${user.name}`)
      setUid('')
      setLabel('')
      const updated = await api.getUserTokens(user.id)
      setTokens(updated)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Assignment failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md">
        <div className="p-6 border-b border-gray-100">
          <h3 className="text-lg font-semibold text-gray-900">Assign NFC Token</h3>
          <p className="text-sm text-gray-500 mt-1">
            Linking a token to <span className="font-medium">{user.name}</span> ({user.email})
          </p>
        </div>

        <div className="p-6 space-y-4">
          {/* Existing tokens */}
          {tokens.length > 0 && (
            <div>
              <p className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-2">
                Already registered ({tokens.length})
              </p>
              <div className="space-y-1">
                {tokens.map(t => (
                  <div key={t.id} className="flex items-center justify-between bg-gray-50 rounded-lg px-3 py-2 text-sm">
                    <span className="font-medium">{t.label ?? t.uid}</span>
                    <span className="text-gray-400 font-mono text-xs">{t.uid}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Assign new token */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              NFC Token UID <span className="text-red-500">*</span>
            </label>
            <input
              value={uid}
              onChange={e => setUid(e.target.value)}
              placeholder="e.g. A3F2B1C4"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <p className="text-xs text-gray-400 mt-1">
              Found by scanning the NFC accessory with any NFC reader app
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Label <span className="text-gray-400 font-normal">(optional)</span>
            </label>
            <input
              value={label}
              onChange={e => setLabel(e.target.value)}
              placeholder="e.g. Bizcotap Ring, Work Card"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {error && <p className="text-sm text-red-600 bg-red-50 rounded-lg px-3 py-2">{error}</p>}
          {success && <p className="text-sm text-green-700 bg-green-50 rounded-lg px-3 py-2">{success}</p>}
        </div>

        <div className="p-6 border-t border-gray-100 flex gap-3 justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-50 transition-colors"
          >
            Close
          </button>
          <button
            onClick={assign}
            disabled={loading || !uid.trim()}
            className="px-4 py-2 text-sm rounded-lg bg-blue-600 text-white font-medium hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {loading ? 'Assigning…' : 'Assign Token'}
          </button>
        </div>
      </div>
    </div>
  )
}
