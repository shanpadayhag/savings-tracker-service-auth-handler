import IUserRepository from '@/core/repositories/user-repository';
import * as schema from '@/infrastructure/database/schema';
import bcrypt from 'bcryptjs';

class RegisterUser {
  constructor(private repository: IUserRepository) { }

  async execute(newUser: schema.NewUser) {
    return this.repository.create({
      ...newUser, password: await bcrypt.hash(newUser.password, 12)
    });
  }
}

export default RegisterUser;
