import LoginUser from '@/core/use-cases/login-user';
import RegisterUser from '@/core/use-cases/register-user';
import { Request, Response } from "express";

class AuthController {
  constructor(
    private registerUser: RegisterUser,
    private loginUser: LoginUser,
  ) {
    this.register = this.register.bind(this);
    this.login = this.login.bind(this);
  }

  async register(request: Request, respond: Response) {
    try {
      const newUser = request.body;
      const user = await this.registerUser.execute(newUser);
      respond.status(201).json(user);
    } catch (error) {
      console.error(error);
      respond.status(500).json({ message: 'Internal server error' });
    }
  }

  async login(request: Request, respond: Response) {
    try {
      const { email, password } = request.body;
      const user = await this.loginUser.execute(email, password);
      respond.status(200).json(user);
    } catch (error) {
      console.error(error);
      respond.status(500).json({ message: 'Internal server error' });
    }
  }
}

export default AuthController;
