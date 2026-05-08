import { Router } from 'express'
import { sendOtpHandler } from './otp.controller'

const router = Router()
router.post('/send', sendOtpHandler)
export default router
