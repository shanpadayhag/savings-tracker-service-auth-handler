import IUserRepository from '@/core/repositories/user-repository';
import * as schema from '@/infrastructure/database/schema';
import { PostgresJsDatabase } from 'drizzle-orm/postgres-js';

class UserRepository implements IUserRepository {
  constructor(private db: PostgresJsDatabase) { }

  async create(user: schema.NewUser): Promise<schema.User> {
    const result = await this.db.insert(schema.users).values(user).returning();

    if (!result || !result[0]) throw new Error('Failed to create user');
    return result[0];
  }
}

export default UserRepository;
