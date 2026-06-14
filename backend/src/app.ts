import express from 'express'
import cors from 'cors'
import helmet from 'helmet'
import rateLimit from 'express-rate-limit'
import { env } from './config/env'
import { errorHandler } from './middleware/error.middleware'
import authRoutes from './modules/auth/auth.routes'
import otpRoutes from './modules/otp/otp.routes'
import userRoutes from './modules/users/user.routes'
import walletRoutes from './modules/wallets/wallet.routes'
import nfcRoutes from './modules/nfc/nfc.routes'
import transactionRoutes from './modules/transactions/transaction.routes'
import analyticsRoutes from './modules/analytics/analytics.routes'
import topupRoutes from './modules/topup/topup.routes'

const app = express()

app.use(helmet())
app.use(cors({ origin: env.CORS_ORIGIN }))
app.use(express.json())
app.use(rateLimit({ windowMs: 15 * 60 * 1000, max: 100, standardHeaders: true }))

app.get('/health', (_req, res) => res.json({ status: 'ok' }))

app.use('/api/auth', authRoutes)
app.use('/api/otp', otpRoutes)
app.use('/api/users', userRoutes)
app.use('/api/wallets', walletRoutes)
app.use('/api/nfc', nfcRoutes)
app.use('/api/transactions', transactionRoutes)
app.use('/api/analytics', analyticsRoutes)
app.use('/api/topup', topupRoutes)

app.use(errorHandler)

export default app
