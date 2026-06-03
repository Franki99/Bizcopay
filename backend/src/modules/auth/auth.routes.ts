import { Router } from 'express'
import * as authController from './auth.controller'
import { authenticate } from '../../middleware/auth.middleware'

const router = Router()

router.post('/register', authController.register)
router.post('/login', authController.login)
router.post('/reset-pin', authController.resetPin)
router.post('/change-pin', authenticate, authController.changePinController)

export default router
