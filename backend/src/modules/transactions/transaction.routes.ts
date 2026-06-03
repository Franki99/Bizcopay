import { Router } from 'express'
import { authenticate, requireRole } from '../../middleware/auth.middleware'
import * as transactionController from './transaction.controller'
import { Role } from '@prisma/client'

const router = Router()

router.use(authenticate)

router.post('/', requireRole(Role.MERCHANT), transactionController.create)
router.get('/my', transactionController.getMy)
router.get('/my/export', transactionController.exportCsv)
router.post('/:id/nfc', requireRole(Role.MERCHANT), transactionController.resolveNfc)
router.post('/:id/approve', requireRole(Role.MERCHANT), transactionController.approveWithPin)
router.patch('/:id/category', requireRole(Role.PAYER), transactionController.categorize)
router.get('/', transactionController.getAll)

export default router
