import { User } from '@/infrastructure/database/schema';
import TokenType from '@/shared/enums/token-type';
import jsonWebToken from '@/config/json-web-token';
import jwt from 'jsonwebtoken';
import type { StringValue } from "ms";

type SignTokenPayload = {
  userID: User['id'];
};

class Jwt {
  signToken(payload: SignTokenPayload, type: TokenType) {
    const tokenTypeIsAccess = type === TokenType.Access;
    const secret = tokenTypeIsAccess
      ? jsonWebToken.ACCESS_SECRET
      : jsonWebToken.REFRESH_SECRET;
    const expiresIn = tokenTypeIsAccess
      ? jsonWebToken.ACCESS_EXPIRES_IN
      : jsonWebToken.REFRESH_EXPIRES_IN;

    return jwt.sign(payload, secret, {
      expiresIn: expiresIn as StringValue,
    });
  }

  verifyToken(value: string, type: TokenType) {
    const secret = type === TokenType.Access
      ? jsonWebToken.ACCESS_SECRET
      : jsonWebToken.REFRESH_SECRET;

    const decoded = jwt.verify(value, secret);

    return decoded;
  }
}

export default Jwt;
