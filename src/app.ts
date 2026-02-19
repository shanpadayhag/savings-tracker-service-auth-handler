import bodyParser from "body-parser";
import cors from "cors";
import express from "express";
import helmet from 'helmet';
import morgan from "morgan";
import createAuthRoutes from '@/presentation/http/routes/auth-routes';
import container from '@/container';

const createApp = (deps = container) => {
  const app = express();

  app.use(express.json());
  app.use(helmet());
  app.use(helmet.crossOriginResourcePolicy({ policy: "cross-origin" }));
  app.use(morgan("common"));
  app.use(bodyParser.json());
  app.use(bodyParser.urlencoded({ extended: false }));
  app.use(cors());

  app.use('/auth', createAuthRoutes(deps.authController));

  return app;
};

export default createApp;
