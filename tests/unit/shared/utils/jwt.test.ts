import { userValid } from '@/fixtures/user';
import TokenType from '@/shared/enums/token-type';
import Jwt from '@/shared/utils/jwt';
import { beforeEach, describe, expect, it } from '@jest/globals';

describe("Jwt", () => {
  let jwt: Jwt;

  beforeEach(() => {
    jwt = new Jwt();
  });

  describe("signToken", () => {
    it("should return a valid JWT string", () => {
      // act
      const token = jwt.signToken(
        { userID: userValid.id },
        TokenType.Access);

      // assert
      expect(typeof token).toBe('string');
      expect(token.split('.')).toHaveLength(3);
    });

    it("should include userId in the payload", () => {
    });

    it("should sign access and refresh tokens with different secrets", () => {
    });
  });

  describe("verifyToken", () => {

  });

  describe("decodeToken", () => {

  });
});
