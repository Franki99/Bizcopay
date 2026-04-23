import { Request, Response, NextFunction } from 'express'
import jwt from 'jsonwebtoken'
import { env } from '../config/env'
import { JwtPayload } from '../types'
import { Role } from '@prisma/client'

export function authenticate(req: Request, res: Response, next: NextFunction) {
  const token = req.headers.authorization?.split(' ')[1]
  if (!token) {
    res.status(401).json({ message: 'No token provided' })
    return
  }
  try {
    req.user = jwt.verify(token, env.JWT_SECRET) as JwtPayload
    next()
  } catch {
    res.status(401).json({ message: 'Invalid or expired token' })
  }
}

export function requireRole(...roles: Role[]) {
  return (req: Request, res: Response, next: NextFunction) => {
    if (!req.user || !roles.includes(req.user.role)) {
      res.status(403).json({ message: 'Insufficient permissions' })
      return
    }
    next()
  }
}
