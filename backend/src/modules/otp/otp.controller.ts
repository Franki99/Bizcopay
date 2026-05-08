import { Request, Response, NextFunction } from 'express'
import { z } from 'zod'
import { OtpPurpose } from '@prisma/client'
import { sendOtp } from './otp.service'

const sendOtpSchema = z.object({
  email: z.string().email(),
  purpose: z.nativeEnum(OtpPurpose),
})

export async function sendOtpHandler(req: Request, res: Response, next: NextFunction) {
  try {
    const { email, purpose } = sendOtpSchema.parse(req.body)
    await sendOtp(email, purpose)
    res.json({ message: 'Verification code sent' })
  } catch (err) {
    next(err)
  }
}
