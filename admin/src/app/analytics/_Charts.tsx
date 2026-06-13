'use client'

import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell,
} from 'recharts'
import type { Transaction } from '@/lib/api'

export interface ChartProps {
  transactions: Transaction[]
}

const STATUS_COLORS: Record<string, string> = {
  APPROVED:     '#16a34a',
  FAILED:       '#dc2626',
  PENDING:      '#ca8a04',
  AWAITING_PIN: '#2563eb',
  EXPIRED:      '#9ca3af',
}

const RISK_COLORS: Record<string, string> = {
  LOW:    '#16a34a',
  MEDIUM: '#ca8a04',
  HIGH:   '#dc2626',
}

function groupByDay(transactions: Transaction[]) {
  const map = new Map<string, { volume: number; count: number }>()
  for (const tx of transactions) {
    if (tx.status !== 'APPROVED') continue
    const date = new Date(tx.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
    const prev = map.get(date) ?? { volume: 0, count: 0 }
    map.set(date, { volume: prev.volume + parseFloat(tx.amount), count: prev.count + 1 })
  }
  return Array.from(map.entries()).map(([date, v]) => ({ date, ...v })).slice(-14)
}

function groupByStatus(transactions: Transaction[]) {
  const map = new Map<string, number>()
  for (const tx of transactions) map.set(tx.status, (map.get(tx.status) ?? 0) + 1)
  return Array.from(map.entries()).map(([name, value]) => ({ name, value }))
}

function groupByRisk(transactions: Transaction[]) {
  const map = new Map<string, number>()
  for (const tx of transactions) map.set(tx.riskScore, (map.get(tx.riskScore) ?? 0) + 1)
  return ['LOW', 'MEDIUM', 'HIGH'].map(name => ({ name, value: map.get(name) ?? 0 }))
}

function topMerchants(transactions: Transaction[]) {
  const map = new Map<string, { name: string; volume: number; count: number }>()
  for (const tx of transactions) {
    if (tx.status !== 'APPROVED') continue
    const prev = map.get(tx.merchant.id) ?? { name: tx.merchant.name, volume: 0, count: 0 }
    map.set(tx.merchant.id, { ...prev, volume: prev.volume + parseFloat(tx.amount), count: prev.count + 1 })
  }
  return Array.from(map.values()).sort((a, b) => b.volume - a.volume).slice(0, 5)
}

export default function Charts({ transactions }: ChartProps) {
  const dailyData   = groupByDay(transactions)
  const statusData  = groupByStatus(transactions)
  const riskData    = groupByRisk(transactions)
  const merchants   = topMerchants(transactions)
  const maxVolume   = merchants[0]?.volume ?? 1

  return (
    <>
      {/* Daily Volume */}
      <div className="mt-6 bg-white rounded-xl border border-gray-100 shadow-sm p-6">
        <h3 className="text-base font-semibold text-gray-800 mb-4">Daily Approved Volume (last 14 days)</h3>
        {dailyData.length === 0 ? (
          <p className="text-sm text-gray-400 py-8 text-center">No approved transactions yet</p>
        ) : (
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={dailyData} margin={{ top: 4, right: 8, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 11, fill: '#9ca3af' }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 11, fill: '#9ca3af' }} axisLine={false} tickLine={false}
                tickFormatter={(v: number) => `RWF ${v}`} />
              <Tooltip
                formatter={(v) => [`RWF ${Number(v).toFixed(2)}`, 'Volume']}
                contentStyle={{ fontSize: 12, borderRadius: 8, border: '1px solid #e5e7eb' }}
              />
              <Bar dataKey="volume" fill="#6366f1" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className="mt-6 grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Status Breakdown */}
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
          <h3 className="text-base font-semibold text-gray-800 mb-4">Transaction Status</h3>
          {statusData.length === 0 ? (
            <p className="text-sm text-gray-400 py-8 text-center">No data</p>
          ) : (
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={statusData} layout="vertical" margin={{ top: 0, right: 8, left: 60, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" horizontal={false} />
                <XAxis type="number" tick={{ fontSize: 11, fill: '#9ca3af' }} axisLine={false} tickLine={false} />
                <YAxis type="category" dataKey="name" tick={{ fontSize: 11, fill: '#6b7280' }} axisLine={false} tickLine={false} />
                <Tooltip
                  formatter={(v) => [v, 'Transactions']}
                  contentStyle={{ fontSize: 12, borderRadius: 8, border: '1px solid #e5e7eb' }}
                />
                <Bar dataKey="value" radius={[0, 4, 4, 0]}>
                  {statusData.map(entry => (
                    <Cell key={entry.name} fill={STATUS_COLORS[entry.name] ?? '#9ca3af'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Risk Distribution */}
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
          <h3 className="text-base font-semibold text-gray-800 mb-4">Risk Score Distribution</h3>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={riskData} margin={{ top: 4, right: 8, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#9ca3af' }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 11, fill: '#9ca3af' }} axisLine={false} tickLine={false} />
              <Tooltip
                formatter={(v: number) => [v, 'Transactions']}
                contentStyle={{ fontSize: 12, borderRadius: 8, border: '1px solid #e5e7eb' }}
              />
              <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                {riskData.map(entry => (
                  <Cell key={entry.name} fill={RISK_COLORS[entry.name] ?? '#9ca3af'} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Top Merchants */}
      <div className="mt-6 bg-white rounded-xl border border-gray-100 shadow-sm p-6">
        <h3 className="text-base font-semibold text-gray-800 mb-4">Top Merchants by Volume</h3>
        {merchants.length === 0 ? (
          <p className="text-sm text-gray-400 py-4 text-center">No approved transactions yet</p>
        ) : (
          <div className="space-y-3">
            {merchants.map((m, i) => (
              <div key={m.name} className="flex items-center gap-4">
                <span className="text-xs font-semibold text-gray-400 w-4">{i + 1}</span>
                <span className="text-sm font-medium text-gray-700 w-36 truncate">{m.name}</span>
                <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div className="h-2 bg-indigo-500 rounded-full"
                    style={{ width: `${(m.volume / maxVolume) * 100}%` }} />
                </div>
                <span className="text-sm font-semibold text-gray-900 w-24 text-right">RWF {m.volume.toFixed(2)}</span>
                <span className="text-xs text-gray-400 w-16 text-right">{m.count} txns</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </>
  )
}
