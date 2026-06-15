import { Request, Response, NextFunction } from 'express'
import { z } from 'zod'
import * as userService from './user.service'

export async function getMe(req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await userService.getMe(req.user!.userId))
  } catch (err) {
    next(err)
  }
}

export async function updateMe(req: Request, res: Response, next: NextFunction) {
  try {
    const { name } = z.object({ name: z.string().min(1).max(100) }).parse(req.body)
    res.json(await userService.updateMe(req.user!.userId, name))
  } catch (err) {
    next(err)
  }
}

export async function getAllUsers(_req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await userService.getAllUsers())
  } catch (err) {
    next(err)
  }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await userService.getUserById(req.params.id))
  } catch (err) {
    next(err)
  }
}

export async function deactivateUser(req: Request, res: Response, next: NextFunction) {
  try {
    await userService.deactivateUser(req.params.id)
    res.json({ message: 'User deactivated' })
  } catch (err) {
    next(err)
  }
}
