import { Router } from 'express'
import * as authController from './auth.controller'

const router = Router()

router.post('/register', authController.register)
router.post('/login', authController.login)
router.post('/reset-pin', authController.resetPin)

export default router
