import { describe, jest, beforeEach, it, expect } from '@jest/globals';
import UserRepository from '@/infrastructure/database/repositories/user-repository';
import * as schema from '@/infrastructure/database/schema';
import { NodePgDatabase } from 'drizzle-orm/node-postgres';

describe('UserRepository', () => {
  let repository: UserRepository;

  const mockDBReturning = jest.fn<() => Promise<schema.User[]>>();
  const mockDBValues = jest.fn((_values: schema.NewUser) => ({ returning: mockDBReturning }));
  const mockDBInsert = jest.fn(() => ({ values: mockDBValues }));

  const mockDB = {
    insert: mockDBInsert,
  } as unknown as NodePgDatabase<typeof schema>;

  beforeEach(() => {
    jest.clearAllMocks();
    repository = new UserRepository(mockDB);
  });

  describe('create', () => {
    it('should create a user and return the created user', async () => {
      // arrange
      const newUser = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        password: 'hashed',
      };
      const expectedUser = {
        id: 'abc-123',
        firstName: newUser['firstName'],
        lastName: newUser['lastName'],
        email: newUser['email'],
        password: newUser['password'],
        createdAt: new Date(),
        updatedAt: new Date(),
      };
      mockDBReturning.mockResolvedValue([expectedUser]);

      // act
      const createdUser = await repository.create(newUser);

      // assert
      expect(mockDBInsert).toHaveBeenCalledTimes(1);
      expect(mockDBValues).toHaveBeenCalledWith(newUser);
      expect(mockDBValues).toHaveBeenCalledTimes(1);
      expect(mockDBReturning).toHaveBeenCalledTimes(1);
      expect(createdUser).toEqual(expectedUser);
    });
  });
});
