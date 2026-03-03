import { NewUser, User } from '@/infrastructure/database/schema';

interface IUserRepository {
  create(user: NewUser): Promise<User>;
  findByEmail(email: User['email']): Promise<User | null>;
};

export default IUserRepository;
