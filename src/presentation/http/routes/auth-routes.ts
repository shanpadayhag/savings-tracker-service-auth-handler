import { Router } from 'express';
import AuthController from '@/presentation/http/controllers/auth-controller';

const createAuthRoutes = (authController: AuthController) => {
  const router = Router();

  router.post('/register', authController.register);
  // router.post("/login", authController.login);
  // router.post("/logout", authController.logout);
  // router.post("/refresh", authController.refresh);

  return router;
};

export default createAuthRoutes;
