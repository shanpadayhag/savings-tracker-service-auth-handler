import { beforeEach, describe, expect, it, jest } from '@jest/globals';
import RegisterUser from '@/core/use-cases/register-user';
import { newUserValid, userValid } from '@/fixtures/user';
import * as schema from '@/infrastructure/database/schema';
import AuthController from '@/presentation/http/controllers/auth-controller';
import createApp from '@/app';
import { type Express } from 'express';
import request from 'supertest';

jest.mock('@/infrastructure/database/drizzle', () => ({
  default: jest.fn(),
}));

const createRegisterUser = (overrides: Partial<RegisterUser> = {}): RegisterUser => ({
  execute: jest.fn<() => Promise<schema.User>>().mockResolvedValue(userValid),
  ...overrides,
} as RegisterUser);

describe('AuthController', () => {
  let app: Express;
  let registerUser: RegisterUser;

  beforeEach(() => {
    jest.clearAllMocks();
    registerUser = createRegisterUser();
    app = createApp({ authController: new AuthController(registerUser) });
  });

  describe('POST /auth/register', () => {
    it('should register the user and return 201', async () => {
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
});
