import { RiskScore, TransactionStatus } from '@prisma/client'
import { env } from '../../config/env'
import { prisma } from '../../lib/prisma'

const ML_TIMEOUT_MS = 3000

// Fallback rule constants (used when ML service is unreachable)
const AMOUNT_THRESHOLD = 500
const RAPID_TX_LIMIT = 3
const RAPID_TX_WINDOW_MS = 60 * 1000
const UNUSUAL_HOUR_START = 0
const UNUSUAL_HOUR_END = 5

export interface FraudResult {
  riskScore: RiskScore
  ruleTriggered: string | null
  details: Record<string, unknown>
}

async function callMlService(
  amount: number,
  hour: number,
  velocity60s: number,
): Promise<{ risk: string; score: number } | null> {
  try {
    const controller = new AbortController()
    const timer = setTimeout(() => controller.abort(), ML_TIMEOUT_MS)
    const response = await fetch(`${env.ML_SERVICE_URL}/predict`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount, hour, velocity_60s: velocity60s }),
      signal: controller.signal,
    })
    clearTimeout(timer)
    if (!response.ok) return null
    return response.json() as Promise<{ risk: string; score: number }>
  } catch {
    return null
  }
}

function ruleBasedEvaluate(amount: number, hour: number, recentCount: number): FraudResult {
  if (amount > AMOUNT_THRESHOLD) {
    return {
      riskScore: RiskScore.HIGH,
      ruleTriggered: 'HIGH_AMOUNT',
      details: { amount, threshold: AMOUNT_THRESHOLD, source: 'rules' },
    }
  }
  if (recentCount >= RAPID_TX_LIMIT) {
    return {
      riskScore: RiskScore.HIGH,
      ruleTriggered: 'RAPID_TRANSACTIONS',
      details: { recentCount, windowSeconds: 60, limit: RAPID_TX_LIMIT, source: 'rules' },
    }
  }
  if (hour >= UNUSUAL_HOUR_START && hour < UNUSUAL_HOUR_END) {
    return {
      riskScore: RiskScore.MEDIUM,
      ruleTriggered: 'UNUSUAL_HOUR',
      details: { hour, range: `${UNUSUAL_HOUR_START}:00-${UNUSUAL_HOUR_END}:00`, source: 'rules' },
    }
  }
  return { riskScore: RiskScore.LOW, ruleTriggered: null, details: { source: 'rules' } }
}

export async function evaluate(payerId: string, amount: number): Promise<FraudResult> {
  const windowStart = new Date(Date.now() - RAPID_TX_WINDOW_MS)
  const recentCount = await prisma.transaction.count({
    where: {
      payerId,
      createdAt: { gte: windowStart },
      status: { in: [TransactionStatus.APPROVED, TransactionStatus.PENDING, TransactionStatus.AWAITING_PIN] },
    },
  })
  const hour = new Date().getHours()

  const ml = await callMlService(amount, hour, recentCount)

  if (ml) {
    const riskScore =
      ml.risk === 'HIGH' ? RiskScore.HIGH : ml.risk === 'MEDIUM' ? RiskScore.MEDIUM : RiskScore.LOW
    return {
      riskScore,
      ruleTriggered: ml.risk !== 'LOW' ? 'ML_MODEL' : null,
      details: { score: ml.score, amount, hour, velocity_60s: recentCount, source: 'ml' },
    }
  }

  // ML service unreachable — fall back to deterministic rules
  return ruleBasedEvaluate(amount, hour, recentCount)
}
