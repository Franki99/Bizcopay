import { Router } from 'express'
import { Role } from '@prisma/client'
import { authenticate, requireRole } from '../../middleware/auth.middleware'
import * as nfcController from './nfc.controller'

const router = Router()

router.use(authenticate)

router.post('/', nfcController.registerToken)
router.get('/', nfcController.getMyTokens)
router.patch('/:id/deactivate', nfcController.deactivateToken)

// Admin-only: assign a token to any user, and view any user's tokens
router.post('/assign', requireRole(Role.ADMIN), nfcController.assignToken)
router.get('/user/:userId', requireRole(Role.ADMIN), nfcController.getUserTokensById)

export default router
