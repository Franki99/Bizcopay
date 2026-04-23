import { z } from 'zod'
import { prisma } from '../../lib/prisma'
import { AppError } from '../../middleware/error.middleware'

export const registerTokenSchema = z.object({
  uid: z.string().min(4).max(64),
  label: z.string().max(50).optional(),
})

export async function registerToken(userId: string, data: z.infer<typeof registerTokenSchema>) {
  const existing = await prisma.nfcToken.findUnique({ where: { uid: data.uid } })
  if (existing) throw new AppError(409, 'NFC token already registered')

  return prisma.nfcToken.create({ data: { uid: data.uid, label: data.label, userId } })
}

export async function lookupByUid(uid: string) {
  const token = await prisma.nfcToken.findUnique({
    where: { uid, isActive: true },
    include: { user: { include: { wallet: true } } },
  })
  if (!token) throw new AppError(404, 'NFC token not found or inactive')
  return token
}

export async function getUserTokens(userId: string) {
  return prisma.nfcToken.findMany({ where: { userId, isActive: true } })
}

export async function deactivateToken(tokenId: string, userId: string) {
  const token = await prisma.nfcToken.findFirst({ where: { id: tokenId, userId } })
  if (!token) throw new AppError(404, 'Token not found')
  return prisma.nfcToken.update({ where: { id: tokenId }, data: { isActive: false } })
}
