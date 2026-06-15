import { Request, Response, NextFunction } from 'express'
import * as topupService from './topup.service'

export async function createRequest(req: Request, res: Response, next: NextFunction) {
  try {
    const data = topupService.createRequestSchema.parse(req.body)
    res.status(201).json(await topupService.createRequest(req.user!.userId, data))
  } catch (err) { next(err) }
}

export async function getAllRequests(req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await topupService.getAllRequests())
  } catch (err) { next(err) }
}

export async function getMyRequests(req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await topupService.getMyRequests(req.user!.userId))
  } catch (err) { next(err) }
}

export async function getByUser(req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await topupService.getRequestsByUser(req.params.userId))
  } catch (err) { next(err) }
}

export async function approveRequest(req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await topupService.approveRequest(req.params.id))
  } catch (err) { next(err) }
}

export async function rejectRequest(req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await topupService.rejectRequest(req.params.id))
  } catch (err) { next(err) }
}
