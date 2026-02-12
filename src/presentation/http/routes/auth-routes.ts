import container from 'container';
import express from "express";

const authRoutes = express.Router();
const authController = container.authController;

authRoutes.post("/register", authController.register);
// authRoutes.post("/login", authController.login);
// authRoutes.post("/logout", authController.logout);
// authRoutes.post("/refresh", authController.refresh);

export default authRoutes;
