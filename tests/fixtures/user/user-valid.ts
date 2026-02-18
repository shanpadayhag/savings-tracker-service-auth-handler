import * as schema from '@/infrastructure/database/schema';

const userValid: schema.User = {
  id: 'user-1',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john@example.com',
  password: 'hashed',
  createdAt: new Date('2026-02-18 11:40:00'),
  updatedAt: new Date('2026-02-18 11:40:00'),
};

export default userValid;
