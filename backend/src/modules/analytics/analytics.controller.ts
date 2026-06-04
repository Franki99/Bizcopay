import { Request, Response, NextFunction } from 'express'
import * as analyticsService from './analytics.service'
import { Role } from '@prisma/client'
import { AppError } from '../../middleware/error.middleware'

export async function payer(req: Request, res: Response, next: NextFunction) {
  try {
    if (req.user!.role !== Role.PAYER) throw new AppError(403, 'Payer only')
    const period = (req.query.period as 'week' | 'month' | 'year') || 'year'
    const data = await analyticsService.getPayerAnalytics(req.user!.userId, period)
    res.json(data)
  } catch (err) { next(err) }
}

export async function merchant(req: Request, res: Response, next: NextFunction) {
  try {
    if (req.user!.role !== Role.MERCHANT) throw new AppError(403, 'Merchant only')
    const period = (req.query.period as 'week' | 'month' | 'year') || 'year'
    const data = await analyticsService.getMerchantAnalytics(req.user!.userId, period)
    res.json(data)
  } catch (err) { next(err) }
}
