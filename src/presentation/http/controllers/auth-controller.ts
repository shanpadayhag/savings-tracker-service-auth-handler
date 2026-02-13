import { Request, Response } from "express";

class AuthController {
  constructor() { }

  register(_request: Request, respond: Response) {
    respond.send("This is register route");
  }
}

export default AuthController;
