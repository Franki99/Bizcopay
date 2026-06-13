'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { api } from '@/lib/api'
import { saveToken } from '@/lib/auth'

type ForgotStep = 'idle' | 'send' | 'reset'

export default function LoginPage() {
  const router = useRouter()

  // Login state
  const [email,   setEmail]   = useState('')
  const [pin,     setPin]     = useState('')
  const [error,   setError]   = useState('')
  const [loading, setLoading] = useState(false)

  // Forgot PIN state
  const [forgotStep,   setForgotStep]   = useState<ForgotStep>('idle')
  const [forgotEmail,  setForgotEmail]  = useState('')
  const [forgotOtp,    setForgotOtp]    = useState('')
  const [forgotPin,    setForgotPin]    = useState('')
  const [forgotPin2,   setForgotPin2]   = useState('')
  const [forgotMsg,    setForgotMsg]    = useState<{ ok: boolean; text: string } | null>(null)
  const [forgotBusy,   setForgotBusy]  = useState(false)

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const { user, token } = await api.login(email, pin)
      if (user.role !== 'ADMIN') { setError('Access denied. Admin accounts only.'); return }
      saveToken(token)
      router.push('/')
    } catch (e: any) {
      setError(e.message ?? 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  async function handleSendOtp(e: React.FormEvent) {
    e.preventDefault()
    setForgotMsg(null)
    setForgotBusy(true)
    try {
      await api.sendResetPinOtp(forgotEmail)
      setForgotStep('reset')
      setForgotMsg({ ok: true, text: 'OTP sent — check your email or the backend terminal.' })
    } catch (err: any) {
      setForgotMsg({ ok: false, text: err.message ?? 'Failed to send OTP.' })
    } finally {
      setForgotBusy(false)
    }
  }

  async function handleResetPin(e: React.FormEvent) {
    e.preventDefault()
    setForgotMsg(null)
    if (forgotPin !== forgotPin2) { setForgotMsg({ ok: false, text: 'PINs do not match.' }); return }
    setForgotBusy(true)
    try {
      await api.resetPin(forgotEmail, forgotOtp, forgotPin)
      setForgotMsg({ ok: true, text: 'PIN reset! You can now sign in.' })
      setTimeout(() => {
        setForgotStep('idle')
        setForgotEmail(''); setForgotOtp(''); setForgotPin(''); setForgotPin2('')
        setForgotMsg(null)
        setEmail(forgotEmail)
      }, 1500)
    } catch (err: any) {
      setForgotMsg({ ok: false, text: err.message ?? 'Failed to reset PIN.' })
    } finally {
      setForgotBusy(false)
    }
  }

  function closeForgot() {
    setForgotStep('idle')
    setForgotEmail(''); setForgotOtp(''); setForgotPin(''); setForgotPin2('')
    setForgotMsg(null)
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-950 via-blue-900 to-blue-800 flex items-center justify-center p-4">
      <div className="w-full max-w-sm">
        {/* Logo */}
        <div className="flex flex-col items-center mb-8">
          <img src="/bizcopay_logo.svg" alt="Bizcopay" className="h-12 w-auto mb-3"
            style={{ filter: 'brightness(0) invert(1)' }} />
          <p className="text-blue-200 text-sm font-medium">Admin Panel</p>
        </div>

        {/* Login card */}
        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <h2 className="text-xl font-bold text-gray-900">Sign in</h2>
          <p className="text-sm text-gray-500 mt-1">Enter your credentials to continue</p>

          <form onSubmit={handleLogin} className="mt-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                type="email" value={email} onChange={e => setEmail(e.target.value)} required
                className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="admin@bizcopay.com"
              />
            </div>
            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="block text-sm font-medium text-gray-700">PIN</label>
                <button type="button" onClick={() => setForgotStep('send')}
                  className="text-xs text-blue-600 hover:text-blue-700 font-medium">
                  Forgot PIN?
                </button>
              </div>
              <input
                type="password" value={pin} onChange={e => setPin(e.target.value)}
                maxLength={4} required inputMode="numeric"
                className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 tracking-widest"
                placeholder="••••"
              />
            </div>

            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg px-3 py-2">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}

            <button type="submit" disabled={loading}
              className="w-full bg-blue-600 text-white py-2.5 rounded-lg text-sm font-semibold hover:bg-blue-700 disabled:opacity-50 transition-colors mt-2">
              {loading ? 'Signing in…' : 'Sign In'}
            </button>
          </form>
        </div>
      </div>

      {/* Forgot PIN modal */}
      {forgotStep !== 'idle' && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-7">
            <div className="flex items-center justify-between mb-5">
              <div>
                <h3 className="text-lg font-bold text-gray-900">Reset PIN</h3>
                <p className="text-xs text-gray-500 mt-0.5">
                  {forgotStep === 'send' ? 'Enter your email to receive a reset code' : 'Enter the code and your new PIN'}
                </p>
              </div>
              <button onClick={closeForgot} className="text-gray-400 hover:text-gray-600 transition-colors">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            {forgotStep === 'send' ? (
              <form onSubmit={handleSendOtp} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Your Email</label>
                  <input
                    type="email" required autoFocus
                    value={forgotEmail} onChange={e => setForgotEmail(e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="admin@bizcopay.com"
                  />
                </div>
                {/* Dev mode hint */}
                <div className="flex items-start gap-2 p-3 bg-blue-50 rounded-lg">
                  <svg className="w-4 h-4 text-blue-500 mt-0.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <p className="text-xs text-blue-700">
                    No email configured? The OTP will appear in the <span className="font-mono font-semibold">backend terminal</span>.
                  </p>
                </div>
                {forgotMsg && (
                  <p className={`text-sm ${forgotMsg.ok ? 'text-green-600' : 'text-red-600'}`}>{forgotMsg.text}</p>
                )}
                <button type="submit" disabled={forgotBusy}
                  className="w-full py-2.5 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors">
                  {forgotBusy ? 'Sending…' : 'Send OTP'}
                </button>
              </form>
            ) : (
              <form onSubmit={handleResetPin} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">OTP Code</label>
                  <input
                    type="text" inputMode="numeric" maxLength={6} required autoFocus
                    value={forgotOtp} onChange={e => setForgotOtp(e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 tracking-widest"
                    placeholder="123456"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">New PIN</label>
                  <input
                    type="password" inputMode="numeric" maxLength={4} required
                    value={forgotPin} onChange={e => setForgotPin(e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 tracking-widest"
                    placeholder="••••"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Confirm New PIN</label>
                  <input
                    type="password" inputMode="numeric" maxLength={4} required
                    value={forgotPin2} onChange={e => setForgotPin2(e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 tracking-widest"
                    placeholder="••••"
                  />
                </div>
                {forgotMsg && (
                  <p className={`text-sm ${forgotMsg.ok ? 'text-green-600' : 'text-red-600'}`}>{forgotMsg.text}</p>
                )}
                <div className="flex gap-3">
                  <button type="submit" disabled={forgotBusy}
                    className="flex-1 py-2.5 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors">
                    {forgotBusy ? 'Resetting…' : 'Reset PIN'}
                  </button>
                  <button type="button" onClick={() => { setForgotStep('send'); setForgotMsg(null) }}
                    className="px-4 py-2.5 bg-gray-100 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-200 transition-colors">
                    Back
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
