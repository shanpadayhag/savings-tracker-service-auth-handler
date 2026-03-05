import IUserRepository from '@/core/repositories/user-repository';
import { User } from '@/infrastructure/database/schema';
import TokenType from '@/shared/enums/token-type';
import InvalidCredentialsError from '@/shared/errors/invalid-credentials-error';
import Jwt from '@/shared/utils/jwt';
import bcrypt from 'bcryptjs';

class LoginUser {
  constructor(
    private repository: IUserRepository,
    private jwt: Jwt,
  ) { }

  async execute(email: User['email'], password: string) {
    const user = await this.repository.findByEmail(email);

    if (!user || !(await bcrypt.compare(password, user.password)))
      throw new InvalidCredentialsError();

    const accessToken = this.jwt.signToken({ userID: user.id }, TokenType.Access);

    return {
      id: user.id,
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      createdAt: user.createdAt,
      updatedAt: user.updatedAt,
      accessToken: accessToken,
    };
  }
}

export default LoginUser;
