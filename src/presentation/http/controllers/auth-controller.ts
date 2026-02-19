import RegisterUser from '@/core/use-cases/register-user';
import { Request, Response } from "express";

class AuthController {
  constructor(
    private registerUser: RegisterUser,
  ) { }

  register = async (request: Request, respond: Response) => {
    try {
      const newUser = request.body;
      const user = await this.registerUser.execute(newUser);
      respond.status(201).json(user);
    } catch (error) {
      console.log(error)
      respond.status(500).json({ message: 'Internal server error' });
    }
  }
}

export default AuthController;
