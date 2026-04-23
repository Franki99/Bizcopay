import { Request, Response, NextFunction } from 'express'
import * as userService from './user.service'

export async function getMe(req: Request, res: Response, next: NextFunction) {
  try {
    res.json(await userService.getMe(req.user!.userId))
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

export async function deactivateUser(req: Request, res: Response, next: NextFunction) {
  try {
    await userService.deactivateUser(req.params.id)
    res.json({ message: 'User deactivated' })
  } catch (err) {
    next(err)
  }
}
