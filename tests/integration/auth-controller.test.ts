import createApp from '@/app';
import IUserRepository from '@/core/repositories/user-repository';
import RegisterUser from '@/core/use-cases/register-user';
import LoginUser from '@/core/use-cases/login-user';
import { newUserValid, userValid } from '@/fixtures/user';
import createUserRepositoryMock from '@/mocks/user-repository.mock';
import AuthController from '@/presentation/http/controllers/auth-controller';
import Jwt from '@/shared/utils/jwt';
import { beforeEach, describe, expect, it, jest } from '@jest/globals';
import { type Express } from 'express';
import request from 'supertest';
import TokenType from '@/shared/enums/token-type';

jest.mock('@/infrastructure/database/drizzle', () => ({
  default: jest.fn(),
}));

describe('AuthController', () => {
  let app: Express;
  let userRepository: IUserRepository;
  let jwt: Jwt;
  let registerUser: RegisterUser;
  let loginUser: LoginUser;

  beforeEach(() => {
    jest.clearAllMocks();
    userRepository = createUserRepositoryMock();
    jwt = new Jwt();
    registerUser = new RegisterUser(userRepository);
    jest.spyOn(registerUser, 'execute');
    loginUser = new LoginUser(userRepository, jwt);
    jest.spyOn(loginUser, 'execute');
    app = createApp({
      authController: new AuthController(
        registerUser,
        loginUser,
      )
    });
  });

  describe('POST /auth/register', () => {
    it('should register the user and return 201', async () => {
      // arrange

      // act
      const res = await request(app)
        .post('/auth/register')
        .send(newUserValid);

      // assert
      expect(res.status).toBe(201);
      expect(res.body).toEqual({
        ...userValid,
        createdAt: userValid.createdAt.toISOString(),
        updatedAt: userValid.updatedAt.toISOString(),
      });
      expect(registerUser.execute).toHaveBeenCalledWith(newUserValid);
      expect(registerUser.execute).toHaveBeenCalledTimes(1);
    });
  });

  describe('POST /auth/login', () => {
    it('should register the user and return 201', async () => {
      // arrange

      // act
      const res = await request(app)
        .post('/auth/login')
        .send({
          email: newUserValid.email,
          password: newUserValid.password,
        });

      // assert
      expect(res.status).toBe(200);
      expect(res.body).toEqual({
        ...userValid,
        password: undefined,
        createdAt: userValid.createdAt.toISOString(),
        updatedAt: userValid.updatedAt.toISOString(),
        accessToken: jwt.signToken({ userID: userValid.id }, TokenType.Access)
      });
      expect(loginUser.execute).toHaveBeenCalledWith(newUserValid.email, newUserValid.password);
      expect(loginUser.execute).toHaveBeenCalledTimes(1);
    });
  });
});
