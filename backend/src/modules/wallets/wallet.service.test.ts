import { prisma } from '../../lib/prisma'
import { getWallet, topUp } from './wallet.service'
import { AppError } from '../../middleware/error.middleware'

jest.mock('../../lib/prisma', () => ({
  prisma: {
    wallet: {
      findUnique: jest.fn(),
      update: jest.fn(),
    },
  },
}))

const mockFindUnique = prisma.wallet.findUnique as jest.Mock
const mockUpdate = prisma.wallet.update as jest.Mock

const baseWallet = { id: 'w-1', userId: 'u-1', balance: '100', currency: 'USD' }

beforeEach(() => jest.clearAllMocks())

describe('getWallet', () => {
  it('returns wallet for valid user', async () => {
    mockFindUnique.mockResolvedValue(baseWallet)

    const result = await getWallet('u-1')

    expect(result).toEqual(baseWallet)
    expect(mockFindUnique).toHaveBeenCalledWith({ where: { userId: 'u-1' } })
  })

  it('throws 404 when wallet not found', async () => {
    mockFindUnique.mockResolvedValue(null)

    await expect(getWallet('bad-id')).rejects.toThrow(AppError)
  })
})

describe('topUp', () => {
  it('increments wallet balance', async () => {
    mockFindUnique.mockResolvedValue(baseWallet)
    mockUpdate.mockResolvedValue({ ...baseWallet, balance: '600' })

    await topUp({ userId: 'u-1', amount: 500 })

    expect(mockUpdate).toHaveBeenCalledWith({
      where: { userId: 'u-1' },
      data: { balance: { increment: 500 } },
    })
  })

  it('throws 404 when wallet not found', async () => {
    mockFindUnique.mockResolvedValue(null)

    await expect(topUp({ userId: 'bad', amount: 100 })).rejects.toThrow(AppError)
  })
})
