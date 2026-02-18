import IUserRepository from '@/core/repositories/user-repository';
import * as schema from '@/infrastructure/database/schema';

class RegisterUser {
  constructor(private repository: IUserRepository) { }

  async execute(newUser: schema.NewUser) {
    return this.repository.create(newUser);
  }
}

export default RegisterUser;
