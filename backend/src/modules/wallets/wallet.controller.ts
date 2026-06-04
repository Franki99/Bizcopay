import { Request, Response, NextFunction } from 'express'
import * as walletService from './wallet.service'
import { getIO } from '../../socket'

export async function getMyWallet(req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await walletService.getWallet(req.user!.userId))
  } catch (err) {
    next(err)
  }
}

export async function topUp(req: Request, res: Response, next: NextFunction) {
  try {
    const data = walletService.topUpSchema.parse(req.body)
    const wallet = await walletService.topUp(data)
    getIO().to(`user:${data.userId}`).emit('wallet:topped_up', {
      amount: data.amount,
      balance: wallet.balance.toString(),
    })
    res.json(wallet)
  } catch (err) {
    next(err)
  }
}
