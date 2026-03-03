import { newUserValid, userValid } from '@/fixtures/user';
import { describe, jest, beforeEach, it, expect } from '@jest/globals';
import UserRepository from '@/infrastructure/database/repositories/user-repository';
import { createDrizzleMock, drizzleMockFns } from '@/mocks/infrastructure/database/drizzle.mock';

const { insert: { mockDBInsert, mockDBReturning, mockDBValues } } = drizzleMockFns;

describe('UserRepository', () => {
  let repository: UserRepository;

  beforeEach(() => {
    jest.clearAllMocks();
    repository = new UserRepository(createDrizzleMock());
  });

  describe('create', () => {
    it('should create a user and return the created user', async () => {
      // arrange
      mockDBReturning.mockResolvedValue([userValid]);

      // act
      const createdUser = await repository.create(newUserValid);

      // assert
      expect(mockDBInsert).toHaveBeenCalledTimes(1);
      expect(mockDBValues).toHaveBeenCalledWith(newUserValid);
      expect(mockDBValues).toHaveBeenCalledTimes(1);
      expect(mockDBReturning).toHaveBeenCalledTimes(1);
      expect(createdUser).toEqual(userValid);
    });
  });

  describe("getByEmail", () => {
    it("should successfuly fetch the user by email", async () => {

    });

    it("should successfuly fetch the user by email", async () => {

    });
  });
});
