import { Router } from 'express'
import { authenticate, requireRole } from '../../middleware/auth.middleware'
import * as walletController from './wallet.controller'
import { Role } from '@prisma/client'

const router = Router()

router.use(authenticate)

router.get('/me', walletController.getMyWallet)
router.post('/top-up', requireRole(Role.ADMIN), walletController.topUp)

export default router
