import { prisma } from '../../lib/prisma'
import { evaluate } from './fraud.service'

jest.mock('../../lib/prisma', () => ({
  prisma: {
    transaction: {
      count: jest.fn(),
    },
  },
}))

const mockCount = prisma.transaction.count as jest.Mock

beforeEach(() => {
  jest.clearAllMocks()
  jest.useRealTimers()
  mockCount.mockResolvedValue(0)
})

describe('evaluate', () => {
  it('returns HIGH for amount above threshold', async () => {
    const result = await evaluate('user-1', 501)

    expect(result.riskScore).toBe('HIGH')
    expect(result.ruleTriggered).toBe('HIGH_AMOUNT')
  })

  it('returns LOW for amount exactly at threshold', async () => {
    jest.useFakeTimers()
    jest.setSystemTime(new Date('2024-01-01T10:00:00'))

    const result = await evaluate('user-1', 500)

    expect(result.riskScore).toBe('LOW')
  })

  it('returns HIGH when payer has 3+ rapid transactions', async () => {
    mockCount.mockResolvedValue(3)

    const result = await evaluate('user-1', 10)

    expect(result.riskScore).toBe('HIGH')
    expect(result.ruleTriggered).toBe('RAPID_TRANSACTIONS')
  })

  it('returns MEDIUM for transactions between midnight and 5am', async () => {
    jest.useFakeTimers()
    jest.setSystemTime(new Date('2024-01-01T02:30:00'))

    const result = await evaluate('user-1', 10)

    expect(result.riskScore).toBe('MEDIUM')
    expect(result.ruleTriggered).toBe('UNUSUAL_HOUR')
  })

  it('returns LOW for normal daytime transaction', async () => {
    jest.useFakeTimers()
    jest.setSystemTime(new Date('2024-01-01T10:00:00'))

    const result = await evaluate('user-1', 100)

    expect(result.riskScore).toBe('LOW')
    expect(result.ruleTriggered).toBeNull()
  })

  it('checks HIGH_AMOUNT before RAPID_TRANSACTIONS', async () => {
    mockCount.mockResolvedValue(10)

    const result = await evaluate('user-1', 600)

    // HIGH_AMOUNT fires first — count should never be called
    expect(result.ruleTriggered).toBe('HIGH_AMOUNT')
    expect(mockCount).not.toHaveBeenCalled()
  })
})
