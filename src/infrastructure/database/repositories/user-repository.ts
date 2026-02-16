import IUserRepository from '@/core/repositories/user-repository';
import db from '@/infrastructure/database/drizzle';
import * as schema from '@/infrastructure/database/schema';
import { NodePgDatabase } from 'drizzle-orm/node-postgres';

class UserRepository implements IUserRepository {
  constructor(private db: NodePgDatabase<typeof schema>) { }

  async create(user: schema.NewUser): Promise<schema.User> {
    const result = await this.db.insert(schema.users).values(user).returning();

    if (!result || !result[0]) throw new Error('Failed to create user');
    return result[0];
  }
}

export default UserRepository;
