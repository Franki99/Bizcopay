import 'dotenv/config'
import { createServer } from 'http'
import app from './app'
import { initSocket } from './socket'
import { env } from './config/env'
import { prisma } from './lib/prisma'

const server = createServer(app)
initSocket(server)

server.listen(env.PORT, async () => {
  await prisma.$connect()
  console.log(`Bizcopay backend running on http://localhost:${env.PORT}`)
  console.log(`Environment: ${env.NODE_ENV}`)
})

process.on('SIGTERM', async () => {
  await prisma.$disconnect()
  server.close()
})

process.on('SIGINT', async () => {
  await prisma.$disconnect()
  server.close()
  process.exit(0)
})
