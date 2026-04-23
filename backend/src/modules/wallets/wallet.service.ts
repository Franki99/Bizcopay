import { z } from 'zod'
import { prisma } from '../../lib/prisma'
import { AppError } from '../../middleware/error.middleware'

export const topUpSchema = z.object({
  userId: z.string().uuid(),
  amount: z.number().positive().max(10000),
})

export async function getWallet(userId: string) {
  const wallet = await prisma.wallet.findUnique({ where: { userId } })
  if (!wallet) throw new AppError(404, 'Wallet not found')
  return wallet
}

export async function topUp(data: z.infer<typeof topUpSchema>) {
  const wallet = await prisma.wallet.findUnique({ where: { userId: data.userId } })
  if (!wallet) throw new AppError(404, 'Wallet not found')

  return prisma.wallet.update({
    where: { userId: data.userId },
    data: { balance: { increment: data.amount } },
  })
}
