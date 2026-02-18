import IUserRepository from '@/core/repositories/user-repository';
import { beforeEach, describe, expect, it, jest } from '@jest/globals';
import * as schema from '@/infrastructure/database/schema';
import RegisterUser from '@/core/use-cases/register-user';
import { newUserValid, userValid } from '@/fixtures/user';

const createUserRepository = (overrides: Partial<IUserRepository> = {}): IUserRepository => ({
  create: jest.fn<() => Promise<schema.User>>().mockResolvedValue(userValid),
  ...overrides,
});

describe("RegisterUser", () => {
  let useCase: RegisterUser;
  let repository: IUserRepository;

  beforeEach(() => {
    jest.clearAllMocks();
    repository = createUserRepository();
    useCase = new RegisterUser(repository);
  });

  describe("execute", () => {
    it("should register the user", async () => {
      // act
      const createdUser = await useCase.execute(newUserValid);

      // assert
      expect(createdUser).toEqual(userValid);
      expect(repository.create).toHaveBeenCalledWith(newUserValid);
      expect(repository.create).toHaveBeenCalledTimes(1);
    });
  });
});
