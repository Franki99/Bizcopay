import { RiskScore, TransactionStatus } from '@prisma/client'
import { prisma } from '../../lib/prisma'

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

export async function evaluate(payerId: string, amount: number): Promise<FraudResult> {
  // Rule 1: Large amount
  if (amount > AMOUNT_THRESHOLD) {
    return {
      riskScore: RiskScore.HIGH,
      ruleTriggered: 'HIGH_AMOUNT',
      details: { amount, threshold: AMOUNT_THRESHOLD },
    }
  }

  // Rule 2: Rapid repeated transactions
  const windowStart = new Date(Date.now() - RAPID_TX_WINDOW_MS)
  const recentCount = await prisma.transaction.count({
    where: {
      payerId,
      createdAt: { gte: windowStart },
      status: { in: [TransactionStatus.APPROVED, TransactionStatus.PENDING, TransactionStatus.AWAITING_PIN] },
    },
  })
  if (recentCount >= RAPID_TX_LIMIT) {
    return {
      riskScore: RiskScore.HIGH,
      ruleTriggered: 'RAPID_TRANSACTIONS',
      details: { recentCount, windowSeconds: 60, limit: RAPID_TX_LIMIT },
    }
  }

  // Rule 3: Unusual hour (medium risk, still requires PIN)
  const hour = new Date().getHours()
  if (hour >= UNUSUAL_HOUR_START && hour < UNUSUAL_HOUR_END) {
    return {
      riskScore: RiskScore.MEDIUM,
      ruleTriggered: 'UNUSUAL_HOUR',
      details: { hour, range: `${UNUSUAL_HOUR_START}:00-${UNUSUAL_HOUR_END}:00` },
    }
  }

  return { riskScore: RiskScore.LOW, ruleTriggered: null, details: {} }
}
