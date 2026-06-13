import { Router } from 'express'
import { authenticate, requireRole } from '../../middleware/auth.middleware'
import * as userController from './user.controller'
import { Role } from '@prisma/client'

const router = Router()

router.use(authenticate)

router.get('/me', userController.getMe)
router.patch('/me', userController.updateMe)
router.get('/', requireRole(Role.ADMIN), userController.getAllUsers)
router.patch('/:id/deactivate', requireRole(Role.ADMIN), userController.deactivateUser)

export default router
