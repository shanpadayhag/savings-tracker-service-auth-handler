import LoginUser from '@/core/use-cases/login-user';
import RegisterUser from '@/core/use-cases/register-user';
import db from '@/infrastructure/database/drizzle';
import UserRepository from '@/infrastructure/database/repositories/user-repository';
import AuthController from '@/presentation/http/controllers/auth-controller';
import Jwt from '@/shared/utils/jwt';

const jwt = new Jwt();

const userRepository = new UserRepository(db);
const registerUser = new RegisterUser(userRepository);
const loginUser = new LoginUser(userRepository, jwt);
const authController = new AuthController(
  registerUser,
  loginUser,
);

const container = {
  authController,
};

export default container;
