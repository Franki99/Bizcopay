'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { api } from '@/lib/api'
import { saveToken } from '@/lib/auth'

type ForgotStep = 'idle' | 'send' | 'reset'

// ─── Eye icon toggle ──────────────────────────────────────────────────────────
function EyeIcon({ open }: { open: boolean }) {
  return open ? (
    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
      <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
    </svg>
  ) : (
    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
    </svg>
  )
}

// ─── Input field ──────────────────────────────────────────────────────────────
function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1.5">{label}</label>
      {children}
    </div>
  )
}

export default function LoginPage() {
  const router = useRouter()

  const [email,      setEmail]      = useState('')
  const [pin,        setPin]        = useState('')
  const [showPin,    setShowPin]    = useState(false)
  const [error,      setError]      = useState('')
  const [loading,    setLoading]    = useState(false)

  const [forgotStep,  setForgotStep]  = useState<ForgotStep>('idle')
  const [forgotEmail, setForgotEmail] = useState('')
  const [forgotOtp,   setForgotOtp]   = useState('')
  const [forgotPin,   setForgotPin]   = useState('')
  const [forgotPin2,  setForgotPin2]  = useState('')
  const [forgotMsg,   setForgotMsg]   = useState<{ ok: boolean; text: string } | null>(null)
  const [forgotBusy,  setForgotBusy]  = useState(false)
  const [showFP1,     setShowFP1]     = useState(false)
  const [showFP2,     setShowFP2]     = useState(false)

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
      setForgotMsg({ ok: true, text: 'PIN reset successfully!' })
      setTimeout(() => {
        closeForgot()
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
    setForgotMsg(null); setShowFP1(false); setShowFP2(false)
  }

  const inputClass = 'w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition bg-gray-50 focus:bg-white'

  return (
    <div className="min-h-screen flex">

      {/* ── Left brand panel ── */}
      <div className="hidden lg:flex lg:w-1/2 bg-blue-950 flex-col relative overflow-hidden">

        {/* Decorative circles */}
        <div className="absolute -top-24 -left-24 w-96 h-96 bg-blue-800/40 rounded-full blur-3xl" />
        <div className="absolute -bottom-32 -right-16 w-[480px] h-[480px] bg-blue-900/50 rounded-full blur-3xl" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-blue-700/20 rounded-full blur-2xl" />

        {/* Grid dots overlay */}
        <div className="absolute inset-0 opacity-[0.07]"
          style={{ backgroundImage: 'radial-gradient(circle, #fff 1px, transparent 1px)', backgroundSize: '28px 28px' }} />

        <div className="relative z-10 flex flex-col h-full p-12">
          {/* Logo */}
          <div className="flex items-center gap-3">
            <img src="/bizcopay_logo.svg" alt="Bizcopay" className="h-9 w-auto"
              style={{ filter: 'brightness(0) invert(1)' }} />
          </div>

          {/* Centre content */}
          <div className="flex-1 flex flex-col justify-center">
            <h1 className="text-4xl font-bold text-white leading-tight">
              The smarter way<br />to manage payments.
            </h1>
            <p className="text-blue-300 mt-4 text-base leading-relaxed max-w-sm">
              Real-time NFC payment oversight, fraud detection, and wallet management — all in one place.
            </p>

            {/* Feature pills */}
            <div className="mt-10 space-y-3">
              {[
                {
                  icon: (
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M8.111 16.404a5.5 5.5 0 017.778 0M12 20h.01m-7.08-7.071c3.904-3.905 10.236-3.905 14.141 0M1.394 9.393c5.857-5.857 15.355-5.857 21.213 0" />
                    </svg>
                  ),
                  label: 'NFC Contactless Payments',
                  sub: 'Tap-and-go transactions via NFC tokens',
                },
                {
                  icon: (
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                    </svg>
                  ),
                  label: 'Real-time Fraud Detection',
                  sub: 'Automated rules engine flags suspicious activity',
                },
                {
                  icon: (
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                  ),
                  label: 'Analytics & Reporting',
                  sub: 'Insights across volume, approval rates, and trends',
                },
              ].map(f => (
                <div key={f.label} className="flex items-start gap-4 bg-white/5 rounded-2xl px-5 py-4 border border-white/10">
                  <div className="w-10 h-10 rounded-xl bg-blue-500/20 text-blue-300 flex items-center justify-center shrink-0">
                    {f.icon}
                  </div>
                  <div>
                    <p className="text-white text-sm font-semibold">{f.label}</p>
                    <p className="text-blue-400 text-xs mt-0.5">{f.sub}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Footer */}
          <p className="text-blue-500 text-xs">© {new Date().getFullYear()} Bizcopay · Final Year Project</p>
        </div>
      </div>

      {/* ── Right form panel ── */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 bg-white">
        <div className="w-full max-w-md">

          {/* Mobile logo */}
          <div className="flex justify-center mb-8 lg:hidden">
            <img src="/bizcopay_logo.svg" alt="Bizcopay" className="h-10 w-auto" />
          </div>

          {/* Heading */}
          <div className="mb-8">
            <h2 className="text-3xl font-bold text-gray-900">Welcome back</h2>
            <p className="text-gray-500 mt-2">Sign in to your admin account</p>
          </div>

          <form onSubmit={handleLogin} className="space-y-5">
            <Field label="Email address">
              <input
                type="email" value={email} onChange={e => setEmail(e.target.value)} required
                autoComplete="email" autoFocus
                className={inputClass}
                placeholder="admin@bizcopay.com"
              />
            </Field>

            <Field label="PIN">
              <div>
                <div className="relative">
                  <input
                    type={showPin ? 'text' : 'password'}
                    value={pin} onChange={e => setPin(e.target.value)}
                    maxLength={4} required inputMode="numeric"
                    autoComplete="current-password"
                    className={`${inputClass} tracking-widest pr-11`}
                    placeholder="••••"
                  />
                  <button type="button" tabIndex={-1}
                    onClick={() => setShowPin(v => !v)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors">
                    <EyeIcon open={showPin} />
                  </button>
                </div>
                <div className="flex justify-end mt-1.5">
                  <button type="button" onClick={() => setForgotStep('send')}
                    className="text-xs text-blue-600 hover:text-blue-700 font-medium">
                    Forgot PIN?
                  </button>
                </div>
              </div>
            </Field>

            {error && (
              <div className="flex items-center gap-2.5 bg-red-50 border border-red-200 rounded-xl px-4 py-3">
                <svg className="w-4 h-4 text-red-500 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}

            <button type="submit" disabled={loading}
              className="w-full bg-blue-600 text-white py-3 rounded-xl text-sm font-semibold hover:bg-blue-700 disabled:opacity-50 transition-colors flex items-center justify-center gap-2 shadow-sm shadow-blue-200 mt-2">
              {loading ? (
                <>
                  <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Signing in…
                </>
              ) : 'Sign In'}
            </button>
          </form>
        </div>
      </div>

      {/* ── Forgot PIN modal ── */}
      {forgotStep !== 'idle' && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50"
          onClick={e => { if (e.target === e.currentTarget) closeForgot() }}>
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm overflow-hidden">

            {/* Modal header */}
            <div className="bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-5">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-lg font-bold text-white">Reset PIN</h3>
                  <p className="text-blue-200 text-xs mt-0.5">
                    {forgotStep === 'send' ? 'Step 1 of 2 — Enter your email' : 'Step 2 of 2 — Enter the code and new PIN'}
                  </p>
                </div>
                <button onClick={closeForgot}
                  className="w-8 h-8 rounded-lg bg-white/10 text-white hover:bg-white/20 transition flex items-center justify-center">
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>

              {/* Step progress */}
              <div className="flex gap-1.5 mt-4">
                <div className="h-1 flex-1 bg-white rounded-full" />
                <div className={`h-1 flex-1 rounded-full transition-colors ${forgotStep === 'reset' ? 'bg-white' : 'bg-white/30'}`} />
              </div>
            </div>

            <div className="p-6">
              {forgotStep === 'send' ? (
                <form onSubmit={handleSendOtp} className="space-y-4">
                  <Field label="Your email address">
                    <input
                      type="email" required autoFocus
                      value={forgotEmail} onChange={e => setForgotEmail(e.target.value)}
                      className={inputClass}
                      placeholder="admin@bizcopay.com"
                    />
                  </Field>

                  {forgotMsg && (
                    <div className={`flex items-center gap-2 rounded-xl px-4 py-3 text-sm ${
                      forgotMsg.ok ? 'bg-green-50 border border-green-200 text-green-700' : 'bg-red-50 border border-red-200 text-red-600'
                    }`}>
                      {forgotMsg.ok
                        ? <svg className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" /></svg>
                        : <svg className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
                      }
                      {forgotMsg.text}
                    </div>
                  )}

                  <button type="submit" disabled={forgotBusy}
                    className="w-full py-3 bg-blue-600 text-white text-sm font-semibold rounded-xl hover:bg-blue-700 disabled:opacity-50 transition-colors flex items-center justify-center gap-2">
                    {forgotBusy ? <><div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />Sending…</> : 'Send OTP'}
                  </button>
                </form>
              ) : (
                <form onSubmit={handleResetPin} className="space-y-4">
                  <Field label="OTP Code">
                    <input
                      type="text" inputMode="numeric" maxLength={6} required autoFocus
                      value={forgotOtp} onChange={e => setForgotOtp(e.target.value)}
                      className={`${inputClass} tracking-widest text-center text-lg font-bold`}
                      placeholder="· · · · · ·"
                    />
                  </Field>

                  <Field label="New PIN">
                    <div className="relative">
                      <input
                        type={showFP1 ? 'text' : 'password'}
                        inputMode="numeric" maxLength={4} required
                        value={forgotPin} onChange={e => setForgotPin(e.target.value)}
                        className={`${inputClass} tracking-widest pr-11`}
                        placeholder="••••"
                      />
                      <button type="button" tabIndex={-1} onClick={() => setShowFP1(v => !v)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                        <EyeIcon open={showFP1} />
                      </button>
                    </div>
                  </Field>

                  <Field label="Confirm New PIN">
                    <div className="relative">
                      <input
                        type={showFP2 ? 'text' : 'password'}
                        inputMode="numeric" maxLength={4} required
                        value={forgotPin2} onChange={e => setForgotPin2(e.target.value)}
                        className={`${inputClass} tracking-widest pr-11`}
                        placeholder="••••"
                      />
                      <button type="button" tabIndex={-1} onClick={() => setShowFP2(v => !v)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                        <EyeIcon open={showFP2} />
                      </button>
                    </div>
                  </Field>

                  {forgotMsg && (
                    <div className={`flex items-center gap-2 rounded-xl px-4 py-3 text-sm ${
                      forgotMsg.ok ? 'bg-green-50 border border-green-200 text-green-700' : 'bg-red-50 border border-red-200 text-red-600'
                    }`}>
                      {forgotMsg.ok
                        ? <svg className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" /></svg>
                        : <svg className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
                      }
                      {forgotMsg.text}
                    </div>
                  )}

                  <div className="flex gap-3 pt-1">
                    <button type="button"
                      onClick={() => { setForgotStep('send'); setForgotMsg(null) }}
                      className="px-4 py-3 bg-gray-100 text-gray-600 text-sm font-semibold rounded-xl hover:bg-gray-200 transition-colors">
                      Back
                    </button>
                    <button type="submit" disabled={forgotBusy}
                      className="flex-1 py-3 bg-blue-600 text-white text-sm font-semibold rounded-xl hover:bg-blue-700 disabled:opacity-50 transition-colors flex items-center justify-center gap-2">
                      {forgotBusy ? <><div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />Resetting…</> : 'Reset PIN'}
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
