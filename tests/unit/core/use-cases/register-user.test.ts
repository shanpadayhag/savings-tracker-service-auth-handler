import IUserRepository from '@/core/repositories/user-repository';
import { beforeEach, describe, it, jest } from '@jest/globals';
import * as schema from '@/infrastructure/database/schema';
import RegisterUser from '@/core/use-cases/register-user';

const createUserRepository = (overrides: Partial<IUserRepository> = {}): IUserRepository => ({
  create: jest.fn<() => Promise<schema.User>>().mockResolvedValue({
    id: 'user-1',
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
    password: 'hashed',
    createdAt: new Date(),
    updatedAt: new Date(),
  }),
  ...overrides,
});

describe("RegisterUser", () => {
  let useCase: RegisterUser;
  let repository: IUserRepository;

  beforeEach(() => {
    jest.clearAllMocks();
    repository = createUserRepository();
    useCase = new RegisterUser();
  });

  describe("execute", () => {
    it("should register the user", async () => {
      // arrange


      // act


      // assert

    });
  });
});
