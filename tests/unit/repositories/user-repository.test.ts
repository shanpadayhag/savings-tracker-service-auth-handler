// tests/unit/repositories/user-repository.test.ts
import { describe, it, expect, jest, beforeEach } from '@jest/globals';
import type { User } from '@/infrastructure/database/schema';

// Create mocks BEFORE jest.mock() calls
const mockReturning = jest.fn<() => Promise<User[]>>();
const mockValues = jest.fn<(values: any) => { returning: typeof mockReturning }>();
const mockInsert = jest.fn<(table: any) => { values: typeof mockValues }>();

// Set up mock implementations
mockValues.mockReturnValue({ returning: mockReturning });
mockInsert.mockReturnValue({ values: mockValues });

// NOW we can use them in jest.mock()
jest.mock('@/infrastructure/database/drizzle', () => ({
  __esModule: true,
  default: {
    insert: mockInsert,
  },
}));

jest.mock('@/infrastructure/database/schema', () => ({
  users: {},
}));

// Import AFTER mocks are set up
import UserRepository from '@/infrastructure/database/repositories/user-repository';

describe('UserRepository Unit Tests', () => {
  let repository: UserRepository;

  beforeEach(() => {
    repository = new UserRepository();
    jest.clearAllMocks();
  });

  describe('create', () => {
    it('should call database insert with correct values and return created user', async () => {
      // Arrange
      const newUser = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        password: 'hashed',
      };

      const expectedUser: User = {
        id: '123',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        password: 'hashed',
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      mockReturning.mockResolvedValue([expectedUser]);

      // Act
      const result = await repository.create(newUser);

      // Assert
      expect(mockInsert).toHaveBeenCalledTimes(1);
      expect(mockValues).toHaveBeenCalledWith(newUser);
      expect(mockReturning).toHaveBeenCalledTimes(1);
      expect(result).toEqual(expectedUser);
    });

    it('should throw error when insert returns empty array', async () => {
      // Arrange
      const newUser = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        password: 'hashed',
      };

      mockReturning.mockResolvedValue([]);

      // Act & Assert
      await expect(repository.create(newUser)).rejects.toThrow('Failed to create user');
    });

    it('should throw error when insert returns null', async () => {
      // Arrange
      const newUser = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        password: 'hashed',
      };

      mockReturning.mockResolvedValue(null as any);

      // Act & Assert
      await expect(repository.create(newUser)).rejects.toThrow('Failed to create user');
    });

    it('should throw error when insert returns undefined', async () => {
      // Arrange
      const newUser = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        password: 'hashed',
      };

      mockReturning.mockResolvedValue(undefined as any);

      // Act & Assert
      await expect(repository.create(newUser)).rejects.toThrow('Failed to create user');
    });

    it('should propagate database errors', async () => {
      // Arrange
      const newUser = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        password: 'hashed',
      };

      const dbError = new Error('Database connection failed');
      mockReturning.mockRejectedValue(dbError);

      // Act & Assert
      await expect(repository.create(newUser)).rejects.toThrow('Database connection failed');
    });
  });
});
