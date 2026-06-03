import { Request, Response, NextFunction } from 'express'
import * as authService from './auth.service'
import { changePinSchema, changePin } from './auth.service'

export async function register(req: Request, res: Response, next: NextFunction) {
  try {
    const data = authService.registerSchema.parse(req.body)
    const result = await authService.register(data)
    res.status(201).json(result)
  } catch (err) {
    next(err)
  }
}

export async function login(req: Request, res: Response, next: NextFunction) {
  try {
    const data = authService.loginSchema.parse(req.body)
    const result = await authService.login(data)
    res.json(result)
  } catch (err) {
    next(err)
  }
}

export async function resetPin(req: Request, res: Response, next: NextFunction) {
  try {
    const data = authService.resetPinSchema.parse(req.body)
    await authService.resetPin(data)
    res.json({ message: 'PIN reset successfully' })
  } catch (err) {
    next(err)
  }
}

export const changePinController = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { currentPin, newPin } = changePinSchema.parse(req.body)
    await changePin(req.user!.userId, currentPin, newPin)
    res.json({ message: 'PIN changed successfully' })
  } catch (e) { next(e) }
}
