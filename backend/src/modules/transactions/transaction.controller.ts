import { Request, Response, NextFunction } from 'express'
import * as transactionService from './transaction.service'

export async function create(req: Request, res: Response, next: NextFunction) {
  try {
    const data = transactionService.createTransactionSchema.parse(req.body)
    res.status(201).json(await transactionService.create(req.user!.userId, data))
  } catch (err) {
    next(err)
  }
}

export async function resolveNfc(req: Request, res: Response, next: NextFunction) {
  try {
    const { nfcUid } = req.body
    if (!nfcUid) { res.status(400).json({ message: 'nfcUid is required' }); return }
    res.json(await transactionService.resolveNfc(req.params.id, nfcUid))
  } catch (err) {
    next(err)
  }
}

export async function approveWithPin(req: Request, res: Response, next: NextFunction) {
  try {
    const { pin } = req.body
    if (!pin) { res.status(400).json({ message: 'pin is required' }); return }
    res.json(await transactionService.approveWithPin(req.params.id, req.user!.userId, pin))
  } catch (err) {
    next(err)
  }
}

export async function getAll(req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await transactionService.getTransactions(req.user!.userId, req.user!.role))
  } catch (err) {
    next(err)
  }
}
