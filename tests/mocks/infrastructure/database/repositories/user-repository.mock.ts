import IUserRepository from '@/core/repositories/user-repository';
import { userValid } from '@/fixtures/user';
import * as schema from '@/infrastructure/database/schema';
import { jest } from '@jest/globals';

const defaults: IUserRepository = {
  create: jest.fn<() => Promise<schema.User>>().mockResolvedValue(userValid),
  findByEmail: jest.fn<(email: schema.User['email']) => Promise<schema.User | null>>().mockResolvedValue(userValid),
};

const createUserRepositoryMock = (
  overrides: Partial<IUserRepository> = {}
): jest.Mocked<IUserRepository> => ({
  ...defaults,
  ...overrides,
} as jest.Mocked<IUserRepository>);

export default createUserRepositoryMock;
