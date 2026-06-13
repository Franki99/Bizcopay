'use client'

import { useEffect, useRef, useState } from 'react'
import { useRouter } from 'next/navigation'
import { api, User } from '@/lib/api'
import PageShell from '@/components/PageShell'

type PinState  = { current: string; next: string; confirm: string }
type EmailState = { newEmail: string; otp: string; otpSent: boolean }
type Msg = { ok: boolean; text: string }

function Avatar({ src, name, size = 20 }: { src: string | null; name?: string; size?: number }) {
  const initials = name ? name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase() : 'A'
  const px = `${size * 4}px`
  return src ? (
    <img src={src} alt="avatar" style={{ width: px, height: px }}
      className="rounded-full object-cover ring-4 ring-blue-100" />
  ) : (
    <div style={{ width: px, height: px }}
      className="rounded-full bg-blue-600 text-white flex items-center justify-center font-bold text-2xl">
      {initials}
    </div>
  )
}

function Msg({ msg }: { msg: Msg | null }) {
  if (!msg) return null
  return (
    <p className={`text-sm ${msg.ok ? 'text-green-600' : 'text-red-600'}`}>{msg.text}</p>
  )
}

export default function ProfilePage() {
  const router  = useRouter()
  const fileRef = useRef<HTMLInputElement>(null)

  const [user,    setUser]    = useState<User | null>(null)
  const [avatar,  setAvatar]  = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  // name editing
  const [editingName, setEditingName] = useState(false)
  const [nameInput,   setNameInput]   = useState('')
  const [nameMsg,     setNameMsg]     = useState<Msg | null>(null)
  const [nameLoading, setNameLoading] = useState(false)

  // PIN
  const [pin,       setPin]       = useState<PinState>({ current: '', next: '', confirm: '' })
  const [pinMsg,    setPinMsg]    = useState<Msg | null>(null)
  const [pinLoading, setPinLoading] = useState(false)

  // Email
  const [email,        setEmail]        = useState<EmailState>({ newEmail: '', otp: '', otpSent: false })
  const [emailMsg,     setEmailMsg]     = useState<Msg | null>(null)
  const [emailLoading, setEmailLoading] = useState(false)

  useEffect(() => {
    api.getMe()
      .then(u => { setUser(u); setNameInput(u.name) })
      .catch(() => router.push('/login'))
      .finally(() => setLoading(false))
    setAvatar(localStorage.getItem('bizcopay_admin_avatar'))
  }, [router])

  // Avatar handlers
  function handleAvatarUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = () => {
      const dataUrl = reader.result as string
      localStorage.setItem('bizcopay_admin_avatar', dataUrl)
      setAvatar(dataUrl)
      window.dispatchEvent(new Event('avatar-updated'))
    }
    reader.readAsDataURL(file)
  }
  function removeAvatar() {
    localStorage.removeItem('bizcopay_admin_avatar')
    setAvatar(null)
    window.dispatchEvent(new Event('avatar-updated'))
    if (fileRef.current) fileRef.current.value = ''
  }

  // Name handlers
  async function saveName(e: React.FormEvent) {
    e.preventDefault()
    setNameMsg(null)
    setNameLoading(true)
    try {
      const updated = await api.updateMe(nameInput)
      setUser(updated)
      setEditingName(false)
      setNameMsg({ ok: true, text: 'Name updated successfully.' })
      window.dispatchEvent(new Event('user-updated'))
    } catch (err: any) {
      setNameMsg({ ok: false, text: err.message ?? 'Failed to update name.' })
    } finally {
      setNameLoading(false)
    }
  }

  // PIN handlers
  async function handlePinChange(e: React.FormEvent) {
    e.preventDefault()
    setPinMsg(null)
    if (pin.next !== pin.confirm) { setPinMsg({ ok: false, text: 'New PINs do not match.' }); return }
    setPinLoading(true)
    try {
      await api.changePin(pin.current, pin.next)
      setPinMsg({ ok: true, text: 'PIN changed successfully.' })
      setPin({ current: '', next: '', confirm: '' })
    } catch (err: any) {
      setPinMsg({ ok: false, text: err.message ?? 'Failed to change PIN.' })
    } finally {
      setPinLoading(false)
    }
  }

  // Email handlers
  async function handleSendOtp(e: React.FormEvent) {
    e.preventDefault()
    setEmailMsg(null)
    setEmailLoading(true)
    try {
      await api.sendChangeEmailOtp(email.newEmail)
      setEmail(s => ({ ...s, otpSent: true }))
      setEmailMsg({ ok: true, text: 'OTP sent. Check your email — or the backend terminal if email is not configured.' })
    } catch (err: any) {
      setEmailMsg({ ok: false, text: err.message ?? 'Failed to send OTP.' })
    } finally {
      setEmailLoading(false)
    }
  }
  async function handleEmailChange(e: React.FormEvent) {
    e.preventDefault()
    setEmailMsg(null)
    setEmailLoading(true)
    try {
      await api.changeEmail(email.newEmail, email.otp)
      setEmailMsg({ ok: true, text: 'Email updated. Redirecting to login…' })
      setTimeout(() => router.push('/login'), 1500)
    } catch (err: any) {
      setEmailMsg({ ok: false, text: err.message ?? 'Failed to change email.' })
    } finally {
      setEmailLoading(false)
    }
  }

  return (
    <PageShell>
      {loading ? (
        <div className="flex h-64 items-center justify-center">
          <div className="w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      ) : (
        <div className="max-w-2xl mx-auto space-y-6">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Profile Settings</h2>
            <p className="text-sm text-gray-500 mt-1">Manage your account information and security</p>
          </div>

          {/* Avatar + Info */}
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
            <h3 className="text-base font-semibold text-gray-800 mb-5">Profile Picture</h3>
            <div className="flex items-center gap-6">
              <Avatar src={avatar} name={user?.name} size={20} />
              <div className="space-y-2">
                <div className="flex gap-2">
                  <button onClick={() => fileRef.current?.click()}
                    className="px-4 py-2 text-sm font-medium bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                    {avatar ? 'Change Photo' : 'Upload Photo'}
                  </button>
                  {avatar && (
                    <button onClick={removeAvatar}
                      className="px-4 py-2 text-sm font-medium bg-red-50 text-red-600 rounded-lg hover:bg-red-100 transition-colors">
                      Remove
                    </button>
                  )}
                </div>
                <p className="text-xs text-gray-400">JPG, PNG or GIF · max 2 MB</p>
                <input ref={fileRef} type="file" accept="image/*" className="hidden" onChange={handleAvatarUpload} />
              </div>
            </div>

            {/* Account Info */}
            <div className="mt-6 pt-5 border-t border-gray-100 space-y-4">
              {/* Editable Name */}
              <div>
                <p className="text-xs font-medium text-gray-400 uppercase tracking-wide mb-1">Full Name</p>
                {editingName ? (
                  <form onSubmit={saveName} className="flex items-center gap-2">
                    <input
                      value={nameInput}
                      onChange={e => setNameInput(e.target.value)}
                      className="flex-1 border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      autoFocus
                    />
                    <button type="submit" disabled={nameLoading}
                      className="px-3 py-1.5 text-sm font-medium bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors">
                      {nameLoading ? '…' : 'Save'}
                    </button>
                    <button type="button" onClick={() => { setEditingName(false); setNameInput(user?.name ?? ''); setNameMsg(null) }}
                      className="px-3 py-1.5 text-sm font-medium bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 transition-colors">
                      Cancel
                    </button>
                  </form>
                ) : (
                  <div className="flex items-center gap-3">
                    <p className="text-sm font-semibold text-gray-900">{user?.name}</p>
                    <button onClick={() => { setEditingName(true); setNameMsg(null) }}
                      className="text-xs text-blue-600 hover:text-blue-700 font-medium">
                      Edit
                    </button>
                  </div>
                )}
                <Msg msg={nameMsg} />
              </div>

              {/* Read-only fields */}
              <div className="grid grid-cols-2 gap-4">
                {[
                  { label: 'Email',  value: user?.email },
                  { label: 'Role',   value: user?.role },
                  { label: 'Status', value: user?.isActive ? 'Active' : 'Inactive' },
                ].map(f => (
                  <div key={f.label}>
                    <p className="text-xs font-medium text-gray-400 uppercase tracking-wide">{f.label}</p>
                    <p className="text-sm font-semibold text-gray-900 mt-0.5">{f.value ?? '—'}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Change PIN */}
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
            <h3 className="text-base font-semibold text-gray-800 mb-1">Change PIN</h3>
            <p className="text-xs text-gray-400 mb-5">Your PIN must be exactly 4 digits.</p>
            <form onSubmit={handlePinChange} className="space-y-4">
              {([['current', 'Current PIN'], ['next', 'New PIN'], ['confirm', 'Confirm New PIN']] as const).map(([key, label]) => (
                <div key={key}>
                  <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
                  <input
                    type="password" inputMode="numeric" maxLength={4} required
                    value={pin[key]}
                    onChange={e => setPin(s => ({ ...s, [key]: e.target.value }))}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 tracking-widest"
                    placeholder="••••"
                  />
                </div>
              ))}
              <Msg msg={pinMsg} />
              <button type="submit" disabled={pinLoading}
                className="w-full py-2.5 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors">
                {pinLoading ? 'Saving…' : 'Save PIN'}
              </button>
            </form>
          </div>

          {/* Change Email */}
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
            <h3 className="text-base font-semibold text-gray-800 mb-1">Change Email</h3>
            <p className="text-xs text-gray-400 mb-5">
              An OTP will be sent to your <span className="font-medium text-gray-600">current email</span> to verify the change.
            </p>

            <form onSubmit={email.otpSent ? handleEmailChange : handleSendOtp} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">New Email Address</label>
                <input
                  type="email" required
                  value={email.newEmail}
                  onChange={e => setEmail(s => ({ ...s, newEmail: e.target.value }))}
                  disabled={email.otpSent}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-50 disabled:text-gray-400"
                  placeholder="new@email.com"
                />
              </div>
              {email.otpSent && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">OTP Code</label>
                  <input
                    type="text" inputMode="numeric" maxLength={6} required
                    value={email.otp}
                    onChange={e => setEmail(s => ({ ...s, otp: e.target.value }))}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 tracking-widest"
                    placeholder="123456"
                  />
                </div>
              )}
              <Msg msg={emailMsg} />
              <div className="flex gap-3">
                <button type="submit" disabled={emailLoading}
                  className="flex-1 py-2.5 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors">
                  {emailLoading ? 'Please wait…' : email.otpSent ? 'Confirm Change' : 'Send OTP'}
                </button>
                {email.otpSent && (
                  <button type="button"
                    onClick={() => { setEmail({ newEmail: '', otp: '', otpSent: false }); setEmailMsg(null) }}
                    className="px-4 py-2.5 bg-gray-100 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-200 transition-colors">
                    Cancel
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>
      )}
    </PageShell>
  )
}
