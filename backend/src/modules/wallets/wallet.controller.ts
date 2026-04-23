import { Request, Response, NextFunction } from 'express'
import * as walletService from './wallet.service'

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
    res.json(await walletService.topUp(data))
  } catch (err) {
    next(err)
  }
}
