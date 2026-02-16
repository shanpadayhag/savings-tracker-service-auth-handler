import { describe, jest, beforeEach } from '@jest/globals';
import UserRepository from '@/infrastructure/database/repositories/user-repository';
import * as schema from '@/infrastructure/database/schema';
import { NodePgDatabase } from 'drizzle-orm/node-postgres';

describe('Testing UserRepository', () => {
  let repository: UserRepository;

  const mockDBReturning = jest.fn();
  const mockDBValues = jest.fn(() => ({ returning: mockDBReturning }));
  const mockDBInsert = jest.fn(() => ({ values: mockDBValues }));

  const mockDB = {
    insert: mockDBInsert,
  } as unknown as NodePgDatabase<typeof schema>;

  beforeEach(() => {
    jest.clearAllMocks();
    repository = new UserRepository(mockDB);
  });

  describe('', () => {

  });
});
