import IUserRepository from '@/core/repositories/user-repository';
import LoginUser from '@/core/use-cases/login-user';
import { newUserValid, userValid } from '@/fixtures/user';
import createUserRepositoryMock from '@/mocks/user-repository.mock';
import Jwt from '@/shared/utils/jwt';
import { beforeEach, describe, expect, it, jest } from '@jest/globals';

describe('LoginUser', () => {
  describe('execute', () => {
    let userRepository: IUserRepository;
    let jwt: Jwt;
    let loginUser: LoginUser;

    beforeEach(() => {
      jest.clearAllMocks();
      userRepository = createUserRepositoryMock();
      jwt = new Jwt();
      loginUser = new LoginUser(userRepository, jwt);
    });

    it('should login the user and return user details and tokens on valid credentials', async () => {
      // arrange

      // act
      const authUser = await loginUser.execute(
        userValid.email,
        newUserValid.password);

      // assert
      expect(authUser).toMatchObject({
        id: userValid.id,
        firstName: userValid.firstName,
        lastName: userValid.lastName,
        email: userValid.email,
      });
      expect(authUser.accessToken).toBeTruthy();
    });
  });
});
