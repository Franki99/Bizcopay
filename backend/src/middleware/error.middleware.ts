import { Request, Response, NextFunction } from 'express'
import { ZodError } from 'zod'

export class AppError extends Error {
  constructor(public statusCode: number, message: string) {
    super(message)
    this.name = 'AppError'
  }
}

export function errorHandler(err: Error, _req: Request, res: Response, _next: NextFunction) {
  if (err instanceof ZodError) {
    res.status(400).json({ message: 'Validation error', errors: err.flatten().fieldErrors })
    return
  }
  if (err instanceof AppError) {
    res.status(err.statusCode).json({ message: err.message })
    return
  }
  console.error(err)
  res.status(500).json({ message: 'Internal server error' })
}
