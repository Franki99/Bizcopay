import { Request, Response, NextFunction } from 'express'
import * as transactionService from './transaction.service'
import { SpendingCategory } from '@prisma/client'

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
    res.json(await transactionService.approveWithPin(req.params.id, pin))
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

export async function getMy(req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await transactionService.getMyTransactions(req.user!.userId, req.user!.role))
  } catch (err) { next(err) }
}

export async function categorize(req: Request, res: Response, next: NextFunction) {
  try {
    const { category } = req.body
    if (!category) { res.status(400).json({ message: 'category is required' }); return }
    res.json(await transactionService.categorizeTransaction(req.params.id, req.user!.userId, category as SpendingCategory))
  } catch (err) { next(err) }
}

export async function exportCsv(req: Request, res: Response, next: NextFunction) {
  try {
    const csv = await transactionService.exportTransactionsCsv(req.user!.userId, req.user!.role)
    res.setHeader('Content-Type', 'text/csv')
    res.setHeader('Content-Disposition', 'attachment; filename="transactions.csv"')
    res.send(csv)
  } catch (err) { next(err) }
}
