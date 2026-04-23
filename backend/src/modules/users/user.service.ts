import { prisma } from '../../lib/prisma'
import { AppError } from '../../middleware/error.middleware'

export async function getMe(userId: string) {
  const user = await prisma.user.findUnique({
    where: { id: userId },
    include: { wallet: true, nfcTokens: { where: { isActive: true } } },
  })
  if (!user) throw new AppError(404, 'User not found')
  const { pin: _pin, ...rest } = user
  return rest
}

export async function getAllUsers() {
  const users = await prisma.user.findMany({ include: { wallet: true } })
  return users.map(({ pin: _pin, ...rest }) => rest)
}

export async function deactivateUser(userId: string) {
  return prisma.user.update({ where: { id: userId }, data: { isActive: false } })
}
