import bcrypt from 'bcryptjs'
import { Role } from '@prisma/client'
import { prisma } from '../../lib/prisma'
import { register, login, verifyPin } from './auth.service'
import { AppError } from '../../middleware/error.middleware'

jest.mock('../../lib/prisma', () => ({
  prisma: {
    user: {
      findUnique: jest.fn(),
      create: jest.fn(),
    },
  },
}))

jest.mock('bcryptjs', () => ({
  hash: jest.fn().mockResolvedValue('hashed-pin'),
  compare: jest.fn(),
}))

jest.mock('jsonwebtoken', () => ({
  sign: jest.fn().mockReturnValue('mock-jwt-token'),
}))

const mockFindUnique = prisma.user.findUnique as jest.Mock
const mockCreate = prisma.user.create as jest.Mock
const mockCompare = bcrypt.compare as jest.Mock

const baseUser = {
  id: 'user-1',
  name: 'Alice',
  email: 'alice@test.com',
  phone: null,
  role: Role.PAYER,
  isActive: true,
  pin: 'hashed-pin',
  createdAt: new Date(),
  updatedAt: new Date(),
}

beforeEach(() => jest.clearAllMocks())

describe('register', () => {
  it('creates user + wallet and returns token', async () => {
    mockFindUnique.mockResolvedValue(null)
    mockCreate.mockResolvedValue({ ...baseUser, wallet: { id: 'w-1', balance: 0, currency: 'USD' } })

    const result = await register({ name: 'Alice', email: 'alice@test.com', pin: '1234', role: Role.PAYER })

    expect(result.token).toBe('mock-jwt-token')
    expect(result.user).not.toHaveProperty('pin')
  })

  it('throws 409 when email already registered', async () => {
    mockFindUnique.mockResolvedValue(baseUser)

    await expect(
      register({ name: 'Alice', email: 'alice@test.com', pin: '1234', role: Role.PAYER }),
    ).rejects.toThrow(AppError)
  })
})

describe('login', () => {
  it('returns token on valid credentials', async () => {
    mockFindUnique.mockResolvedValue(baseUser)
    mockCompare.mockResolvedValue(true)

    const result = await login({ email: 'alice@test.com', pin: '1234' })

    expect(result.token).toBe('mock-jwt-token')
    expect(result.user).not.toHaveProperty('pin')
  })

  it('throws 401 when user not found', async () => {
    mockFindUnique.mockResolvedValue(null)

    await expect(login({ email: 'nobody@test.com', pin: '1234' })).rejects.toThrow(AppError)
  })

  it('throws 401 when pin is incorrect', async () => {
    mockFindUnique.mockResolvedValue(baseUser)
    mockCompare.mockResolvedValue(false)

    await expect(login({ email: 'alice@test.com', pin: '9999' })).rejects.toThrow(AppError)
  })

  it('throws 401 when user is deactivated', async () => {
    mockFindUnique.mockResolvedValue({ ...baseUser, isActive: false })
    mockCompare.mockResolvedValue(true)

    await expect(login({ email: 'alice@test.com', pin: '1234' })).rejects.toThrow(AppError)
  })
})

describe('verifyPin', () => {
  it('returns true for correct pin', async () => {
    mockFindUnique.mockResolvedValue(baseUser)
    mockCompare.mockResolvedValue(true)

    expect(await verifyPin('user-1', '1234')).toBe(true)
  })

  it('returns false for wrong pin', async () => {
    mockFindUnique.mockResolvedValue(baseUser)
    mockCompare.mockResolvedValue(false)

    expect(await verifyPin('user-1', '9999')).toBe(false)
  })

  it('throws 404 when user not found', async () => {
    mockFindUnique.mockResolvedValue(null)

    await expect(verifyPin('bad-id', '1234')).rejects.toThrow(AppError)
  })
})
