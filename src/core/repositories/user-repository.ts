import { NewUser, User } from '@/infrastructure/database/schema';

interface IUserRepository {
  create(user: NewUser): Promise<User>;
  getByEmail(email: User['email']): Promise<User>;
};

export default IUserRepository;
