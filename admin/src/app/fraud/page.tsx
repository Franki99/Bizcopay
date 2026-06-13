'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { api, Transaction } from '@/lib/api'
import PageShell from '@/components/PageShell'

export default function FraudPage() {
  const router = useRouter()
  const [flagged, setFlagged] = useState<Transaction[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.getTransactions()
      .then(txs => setFlagged(txs.filter(t => t.fraudLog)))
      .catch(() => router.push('/login'))
      .finally(() => setLoading(false))
  }, [router])

  const riskColor: Record<string, string> = {
    HIGH:   'bg-red-100 text-red-700 border-red-200',
    MEDIUM: 'bg-yellow-100 text-yellow-700 border-yellow-200',
  }

  return (
    <PageShell>
      {loading ? (
        <div className="flex h-64 items-center justify-center">
          <div className="w-8 h-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      ) : (
        <>
          <div className="mb-6">
            <h2 className="text-2xl font-bold text-gray-900">Fraud Alerts</h2>
            <p className="text-sm text-gray-500 mt-1">
              {flagged.length} transaction{flagged.length !== 1 ? 's' : ''} flagged
            </p>
          </div>

          {flagged.length === 0 ? (
            <div className="mt-12 text-center">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
                <svg className="w-8 h-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <p className="text-gray-500 mt-3 font-medium">No fraud alerts — all clear.</p>
            </div>
          ) : (
            <div className="space-y-4">
              {flagged.map(tx => (
                <div key={tx.id}
                  className={`rounded-xl border p-5 ${riskColor[tx.fraudLog!.riskScore] ?? 'bg-gray-50 border-gray-200'}`}>
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-semibold text-sm">{tx.fraudLog!.ruleTriggered}</span>
                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                          tx.fraudLog!.riskScore === 'HIGH'
                            ? 'bg-red-200 text-red-800'
                            : 'bg-yellow-200 text-yellow-800'
                        }`}>
                          {tx.fraudLog!.riskScore}
                        </span>
                      </div>
                      <p className="text-xs mt-1">
                        Merchant: <span className="font-medium">{tx.merchant.name}</span>
                        {tx.payer && <> · Payer: <span className="font-medium">{tx.payer.name}</span></>}
                        · Amount: <span className="font-medium">RWF {tx.amount}</span>
                      </p>
                      <p className="text-xs mt-0.5 opacity-70">
                        Details: {JSON.stringify(tx.fraudLog!.details)}
                      </p>
                    </div>
                    <div className="text-right text-xs opacity-60 shrink-0 ml-4">
                      <p className="font-medium">{tx.status}</p>
                      <p>{new Date(tx.createdAt).toLocaleString()}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </PageShell>
  )
}
