import { Request, Response, NextFunction } from 'express'
import * as nfcService from './nfc.service'

export async function registerToken(req: Request, res: Response, next: NextFunction) {
  try {
    const data = nfcService.registerTokenSchema.parse(req.body)
    res.status(201).json(await nfcService.registerToken(req.user!.userId, data))
  } catch (err) {
    next(err)
  }
}

export async function getMyTokens(req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await nfcService.getUserTokens(req.user!.userId))
  } catch (err) {
    next(err)
  }
}

export async function deactivateToken(req: Request, res: Response, next: NextFunction) {
  try {
    await nfcService.deactivateToken(req.params.id, req.user!.userId)
    res.json({ message: 'Token deactivated' })
  } catch (err) {
    next(err)
  }
}
