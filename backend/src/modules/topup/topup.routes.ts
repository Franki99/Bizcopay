import { Router } from 'express'
import { Role } from '@prisma/client'
import { authenticate, requireRole } from '../../middleware/auth.middleware'
import * as topupController from './topup.controller'

const router = Router()
router.use(authenticate)

// Payer routes
router.post('/',      requireRole(Role.PAYER), topupController.createRequest)
router.get('/mine',   requireRole(Role.PAYER), topupController.getMyRequests)

// Admin routes
router.get('/',                    requireRole(Role.ADMIN), topupController.getAllRequests)
router.get('/user/:userId',        requireRole(Role.ADMIN), topupController.getByUser)
router.post('/:id/approve',        requireRole(Role.ADMIN), topupController.approveRequest)
router.post('/:id/reject',         requireRole(Role.ADMIN), topupController.rejectRequest)

export default router
