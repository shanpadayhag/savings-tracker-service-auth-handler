import IUserRepository from '@/core/repositories/user-repository';
import { beforeEach, describe, expect, it, jest } from '@jest/globals';
import * as schema from '@/infrastructure/database/schema';
import RegisterUser from '@/core/use-cases/register-user';
import { newUserValid, userValid } from '@/fixtures/user';
import createUserRepositoryMock from '@/mocks/infrastructure/database/repositories/user-repository.mock';
import bcrypt from 'bcryptjs';

describe("RegisterUser", () => {
  let useCase: RegisterUser;
  let repository: IUserRepository;

  beforeEach(() => {
    jest.clearAllMocks();
    repository = createUserRepositoryMock();
    useCase = new RegisterUser(repository);
  });

  describe("execute", () => {
    it("should register the user", async () => {
      // act
      const createdUser = await useCase.execute(newUserValid);

      // assert
      expect(createdUser).toEqual(userValid);
      expect(repository.create).toHaveBeenCalledTimes(1);

      const repositoryCreateCall = (repository.create as jest.Mock).mock.calls[0];
      expect(repositoryCreateCall).toBeDefined();
      const repositoryCreateCalledWith = repositoryCreateCall![0] as schema.NewUser;

      expect(repositoryCreateCalledWith.firstName).toBe(newUserValid.firstName);
      expect(repositoryCreateCalledWith.lastName).toBe(newUserValid.lastName);
      expect(repositoryCreateCalledWith.email).toBe(newUserValid.email);
      await expect(bcrypt.compare(
        newUserValid.password,
        repositoryCreateCalledWith.password,
      )).resolves.toBe(true);
    });
  });
});
