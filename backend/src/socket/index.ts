import { Server as HttpServer } from 'http'
import { Server as SocketServer } from 'socket.io'
import jwt from 'jsonwebtoken'
import { env } from '../config/env'
import { JwtPayload } from '../types'

let io: SocketServer

export function initSocket(server: HttpServer) {
  io = new SocketServer(server, {
    cors: { origin: env.CORS_ORIGIN, methods: ['GET', 'POST'] },
  })

  io.use((socket, next) => {
    const token = socket.handshake.auth?.token as string | undefined
    if (!token) return next(new Error('Authentication required'))
    try {
      socket.data.user = jwt.verify(token, env.JWT_SECRET) as JwtPayload
      next()
    } catch {
      next(new Error('Invalid token'))
    }
  })

  io.on('connection', (socket) => {
    const user = socket.data.user as JwtPayload
    // Each user gets their own room for direct notifications
    socket.join(`user:${user.userId}`)

    socket.on('join:transaction', (transactionId: string) => {
      socket.join(`transaction:${transactionId}`)
    })

    socket.on('leave:transaction', (transactionId: string) => {
      socket.leave(`transaction:${transactionId}`)
    })

    socket.on('disconnect', () => {
      console.log(`User ${user.userId} disconnected`)
    })
  })

  return io
}

export function getIO(): SocketServer {
  if (!io) throw new Error('Socket.IO not initialized')
  return io
}
