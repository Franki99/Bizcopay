const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:3000'

function getToken(): string | null {
  if (typeof window === 'undefined') return null
  return localStorage.getItem('admin_token')
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const token = getToken()
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options?.headers,
    },
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message ?? `Request failed: ${res.status}`)
  }
  return res.json() as Promise<T>
}

export interface User {
  id: string
  name: string
  email: string
  role: string
  isActive: boolean
  createdAt: string
  wallet?: { balance: string; currency: string }
}

export interface Transaction {
  id: string
  amount: string
  currency: string
  status: string
  riskScore: string
  description?: string
  createdAt: string
  payer?: { id: string; name: string; email: string }
  merchant: { id: string; name: string; email: string }
  fraudLog?: { ruleTriggered: string; riskScore: string; details: Record<string, unknown> }
}

export interface NfcToken {
  id: string
  uid: string
  label: string | null
  isActive: boolean
}

export const api = {
  login: (email: string, pin: string) =>
    request<{ user: User; token: string }>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, pin }),
    }),

  getUsers: () => request<User[]>('/api/users'),

  deactivateUser: (userId: string) =>
    request(`/api/users/${userId}/deactivate`, { method: 'PATCH' }),

  topUp: (userId: string, amount: number) =>
    request('/api/wallets/top-up', {
      method: 'POST',
      body: JSON.stringify({ userId, amount }),
    }),

  getTransactions: () => request<Transaction[]>('/api/transactions'),

  getUserTokens: (userId: string) =>
    request<NfcToken[]>(`/api/nfc/user/${userId}`),

  assignNfcToken: (userId: string, uid: string, label: string) =>
    request<NfcToken>('/api/nfc/assign', {
      method: 'POST',
      body: JSON.stringify({ userId, uid, label: label || undefined }),
    }),
}
