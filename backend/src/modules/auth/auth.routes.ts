import { Router } from 'express'
import * as authController from './auth.controller'
import { authenticate } from '../../middleware/auth.middleware'

const router = Router()

router.post('/register', authController.register)
router.post('/login', authController.login)
router.post('/reset-pin', authController.resetPin)
router.post('/change-pin', authenticate, authController.changePinController)
router.post('/send-change-email-otp', authenticate, authController.sendChangeEmailOtpController)
router.post('/change-email', authenticate, authController.changeEmailController)

export default router
