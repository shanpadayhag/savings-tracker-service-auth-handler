import IUserRepository from '@/core/repositories/user-repository';
import db from '@/infrastructure/database/drizzle';
import { NewUser, User, users } from '@/infrastructure/database/schema';

class UserRepository implements IUserRepository {
  async create(user: NewUser): Promise<User> {
    const result = await db.insert(users).values(user).returning();

    if (!result || !result[0]) throw new Error('Failed to create user');
    return result[0];
  }
}

export default UserRepository;
