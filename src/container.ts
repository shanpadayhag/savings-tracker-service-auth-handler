import db from '@/infrastructure/database/drizzle';
import UserRepository from '@/infrastructure/database/repositories/user-repository';
import AuthController from '@/presentation/http/controllers/auth-controller';

const userRepository = new UserRepository(db);
const authController = new AuthController();

const container = {
  authController,
};

export default container;
