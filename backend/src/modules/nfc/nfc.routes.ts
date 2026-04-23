import { Router } from 'express'
import { authenticate } from '../../middleware/auth.middleware'
import * as nfcController from './nfc.controller'

const router = Router()

router.use(authenticate)

router.post('/', nfcController.registerToken)
router.get('/', nfcController.getMyTokens)
router.patch('/:id/deactivate', nfcController.deactivateToken)

export default router
