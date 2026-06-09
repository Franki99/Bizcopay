import { Request, Response, NextFunction } from 'express'
import * as authService from './auth.service'
import { changePinSchema, changePin, sendChangeEmailOtpSchema, sendChangeEmailOtp, changeEmailSchema, changeEmail } from './auth.service'

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

export const sendChangeEmailOtpController = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { newEmail } = sendChangeEmailOtpSchema.parse(req.body)
    await sendChangeEmailOtp(req.user!.userId, newEmail)
    res.json({ message: 'Verification code sent' })
  } catch (e) { next(e) }
}

export const changeEmailController = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { newEmail, otpCode } = changeEmailSchema.parse(req.body)
    await changeEmail(req.user!.userId, newEmail, otpCode)
    res.json({ message: 'Email updated successfully' })
  } catch (e) { next(e) }
}
