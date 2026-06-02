import { RiskScore, TransactionStatus, Role } from '@prisma/client'
import { prisma } from '../../lib/prisma'
import * as fraudService from '../fraud/fraud.service'
import * as authService from '../auth/auth.service'
import { create, resolveNfc, approveWithPin } from './transaction.service'
import { AppError } from '../../middleware/error.middleware'

jest.mock('../../lib/prisma', () => ({
  prisma: {
    transaction: { findUnique: jest.fn(), create: jest.fn(), update: jest.fn() },
    nfcToken: { findUnique: jest.fn() },
    fraudLog: { create: jest.fn() },
    wallet: { findUnique: jest.fn(), update: jest.fn() },
    user: { findUnique: jest.fn() },
    $transaction: jest.fn(),
  },
}))

jest.mock('../fraud/fraud.service')
jest.mock('../auth/auth.service')
jest.mock('../../socket', () => ({
  getIO: jest.fn().mockReturnValue({
    to: jest.fn().mockReturnValue({ emit: jest.fn() }),
  }),
}))

const mockTxFindUnique = prisma.transaction.findUnique as jest.Mock
const mockTxCreate = prisma.transaction.create as jest.Mock
const mockTxUpdate = prisma.transaction.update as jest.Mock
const mockNfcFindUnique = prisma.nfcToken.findUnique as jest.Mock
const mockFraudLogCreate = prisma.fraudLog.create as jest.Mock
const mockPrismaTransaction = prisma.$transaction as jest.Mock
const mockEvaluate = fraudService.evaluate as jest.Mock
const mockVerifyPin = authService.verifyPin as jest.Mock

const baseMerchant = { id: 'merchant-1', name: 'Bob Shop' }
const basePayer = {
  id: 'payer-1',
  name: 'Alice',
  wallet: { balance: '500' },
}
const baseTransaction = {
  id: 'tx-1',
  amount: '20',
  merchantId: 'merchant-1',
  merchant: baseMerchant,
  status: TransactionStatus.PENDING,
  expiresAt: new Date(Date.now() + 60_000),
}

beforeEach(() => jest.clearAllMocks())

describe('create', () => {
  it('creates a transaction with 5-minute expiry', async () => {
    mockTxCreate.mockResolvedValue({ id: 'tx-1', amount: 20 })

    const result = await create('merchant-1', { amount: 20, description: 'Coffee' })

    expect(mockTxCreate).toHaveBeenCalledWith(
      expect.objectContaining({
        data: expect.objectContaining({ merchantId: 'merchant-1', amount: 20 }),
      }),
    )
    expect(result.id).toBe('tx-1')
  })
})

describe('resolveNfc', () => {
  beforeEach(() => {
    mockTxFindUnique.mockResolvedValue(baseTransaction)
    mockNfcFindUnique.mockResolvedValue({ uid: 'CARD-001', user: basePayer })
    mockTxUpdate.mockResolvedValue({})
    mockFraudLogCreate.mockResolvedValue({})
  })

  it('auto-approves LOW risk transaction with sufficient balance', async () => {
    mockEvaluate.mockResolvedValue({ riskScore: RiskScore.LOW, ruleTriggered: null, details: {} })
    mockPrismaTransaction.mockResolvedValue([])

    const result = await resolveNfc('tx-1', 'CARD-001')

    expect(result.status).toBe(TransactionStatus.APPROVED)
    expect(mockPrismaTransaction).toHaveBeenCalled()
  })

  it('fails LOW risk transaction when balance is insufficient', async () => {
    mockEvaluate.mockResolvedValue({ riskScore: RiskScore.LOW, ruleTriggered: null, details: {} })
    mockNfcFindUnique.mockResolvedValue({
      uid: 'CARD-001',
      user: { ...basePayer, wallet: { balance: '5' } },
    })

    const result = await resolveNfc('tx-1', 'CARD-001')

    expect(result.status).toBe(TransactionStatus.FAILED)
  })

  it('requires PIN for MEDIUM risk transaction', async () => {
    mockEvaluate.mockResolvedValue({
      riskScore: RiskScore.MEDIUM,
      ruleTriggered: 'UNUSUAL_HOUR',
      details: { hour: 2 },
    })

    const result = await resolveNfc('tx-1', 'CARD-001')

    expect(result.status).toBe(TransactionStatus.AWAITING_PIN)
  })

  it('blocks HIGH risk transaction immediately', async () => {
    mockEvaluate.mockResolvedValue({
      riskScore: RiskScore.HIGH,
      ruleTriggered: 'HIGH_AMOUNT',
      details: { amount: 600 },
    })

    const result = await resolveNfc('tx-1', 'CARD-001')

    expect(result.status).toBe(TransactionStatus.FAILED)
    expect(mockFraudLogCreate).toHaveBeenCalled()
  })

  it('throws 400 when transaction is already resolved', async () => {
    mockTxFindUnique.mockResolvedValue({ ...baseTransaction, status: TransactionStatus.APPROVED })

    await expect(resolveNfc('tx-1', 'CARD-001')).rejects.toThrow(AppError)
  })

  it('throws 400 when transaction is expired', async () => {
    mockTxFindUnique.mockResolvedValue({
      ...baseTransaction,
      expiresAt: new Date(Date.now() - 1000),
    })

    await expect(resolveNfc('tx-1', 'CARD-001')).rejects.toThrow(AppError)
  })

  it('throws 404 when NFC token not found', async () => {
    mockNfcFindUnique.mockResolvedValue(null)
    mockEvaluate.mockResolvedValue({ riskScore: RiskScore.LOW, ruleTriggered: null, details: {} })

    await expect(resolveNfc('tx-1', 'UNKNOWN-UID')).rejects.toThrow(AppError)
  })
})

describe('approveWithPin', () => {
  const awaitingTx = {
    ...baseTransaction,
    status: TransactionStatus.AWAITING_PIN,
    payerId: 'payer-1',
  }

  beforeEach(() => {
    mockTxFindUnique.mockResolvedValue(awaitingTx)
    mockVerifyPin.mockResolvedValue(true)
    prisma.user.findUnique = jest.fn().mockResolvedValue(basePayer)
    mockPrismaTransaction.mockResolvedValue([])
  })

  it('approves transaction with correct PIN', async () => {
    const result = await approveWithPin('tx-1', '1234')

    expect(result.status).toBe(TransactionStatus.APPROVED)
    expect(mockPrismaTransaction).toHaveBeenCalled()
  })

  it('throws 401 for wrong PIN', async () => {
    mockVerifyPin.mockResolvedValue(false)

    await expect(approveWithPin('tx-1', '9999')).rejects.toThrow(AppError)
  })

  it('throws 400 when transaction is not awaiting PIN', async () => {
    mockTxFindUnique.mockResolvedValue({ ...awaitingTx, status: TransactionStatus.APPROVED })

    await expect(approveWithPin('tx-1', '1234')).rejects.toThrow(AppError)
  })
})
