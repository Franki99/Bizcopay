import bcrypt from 'bcryptjs'
import { OtpPurpose } from '@prisma/client'
import { prisma } from '../../lib/prisma'
import { sendOtpEmail } from '../../lib/email'
import { AppError } from '../../middleware/error.middleware'

const OTP_EXPIRY_MS = 10 * 60 * 1000

function generateCode(): string {
  return Math.floor(100000 + Math.random() * 900000).toString()
}

export async function sendOtp(email: string, purpose: OtpPurpose) {
  await prisma.otpCode.updateMany({
    where: { email, purpose, used: false },
    data: { used: true },
  })

  const code = generateCode()
  const codeHash = await bcrypt.hash(code, 10)

  await prisma.otpCode.create({
    data: { email, codeHash, purpose, expiresAt: new Date(Date.now() + OTP_EXPIRY_MS) },
  })

  await sendOtpEmail(email, code, purpose)
}

export async function verifyOtp(email: string, code: string, purpose: OtpPurpose) {
  const otp = await prisma.otpCode.findFirst({
    where: { email, purpose, used: false, expiresAt: { gt: new Date() } },
    orderBy: { createdAt: 'desc' },
  })

  if (!otp) throw new AppError(400, 'Invalid or expired verification code')

  const valid = await bcrypt.compare(code, otp.codeHash)
  if (!valid) throw new AppError(400, 'Invalid or expired verification code')

  await prisma.otpCode.update({ where: { id: otp.id }, data: { used: true } })
}
