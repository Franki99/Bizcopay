import bcrypt from 'bcryptjs'
import jwt from 'jsonwebtoken'
import { z } from 'zod'
import { Role } from '@prisma/client'
import { prisma } from '../../lib/prisma'
import { env } from '../../config/env'
import { AppError } from '../../middleware/error.middleware'

export const registerSchema = z.object({
  name: z.string().min(2),
  email: z.string().email(),
  phone: z.string().optional(),
  pin: z.string().length(4).regex(/^\d+$/, 'PIN must be 4 digits'),
  role: z.nativeEnum(Role).default(Role.PAYER),
})

export const loginSchema = z.object({
  email: z.string().email(),
  pin: z.string().length(4),
})

export async function register(data: z.infer<typeof registerSchema>) {
  const exists = await prisma.user.findUnique({ where: { email: data.email } })
  if (exists) throw new AppError(409, 'Email already registered')

  const hashedPin = await bcrypt.hash(data.pin, 10)

  const user = await prisma.user.create({
    data: {
      name: data.name,
      email: data.email,
      phone: data.phone,
      pin: hashedPin,
      role: data.role,
      wallet: { create: { currency: 'USD' } },
    },
    include: { wallet: true },
  })

  return { user: sanitize(user), token: signToken(user.id, user.role) }
}

export async function login(data: z.infer<typeof loginSchema>) {
  const user = await prisma.user.findUnique({ where: { email: data.email } })
  if (!user || !user.isActive) throw new AppError(401, 'Invalid credentials')

  const valid = await bcrypt.compare(data.pin, user.pin)
  if (!valid) throw new AppError(401, 'Invalid credentials')

  return { user: sanitize(user), token: signToken(user.id, user.role) }
}

export async function verifyPin(userId: string, pin: string): Promise<boolean> {
  const user = await prisma.user.findUnique({ where: { id: userId } })
  if (!user) throw new AppError(404, 'User not found')
  return bcrypt.compare(pin, user.pin)
}

function signToken(userId: string, role: Role) {
  return jwt.sign({ userId, role }, env.JWT_SECRET, { expiresIn: '7d' })
}

function sanitize(user: { pin: string; [key: string]: unknown }) {
  const { pin: _pin, ...rest } = user
  return rest
}
