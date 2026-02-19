import RegisterUser from '@/core/use-cases/register-user';
import db from '@/infrastructure/database/drizzle';
import UserRepository from '@/infrastructure/database/repositories/user-repository';
import AuthController from '@/presentation/http/controllers/auth-controller';

const userRepository = new UserRepository(db);
const registerUser = new RegisterUser(userRepository);
const authController = new AuthController(
  registerUser,
);

const container = {
  authController,
};

export default container;
