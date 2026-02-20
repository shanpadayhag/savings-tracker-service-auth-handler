import * as schema from '@/infrastructure/database/schema';

const userValid: schema.User = {
  id: 'user-1',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john@example.com',
  password: '$2b$12$FvBSEya42Lxwrfc8aG3KcuHkswH3uOtSdfkJSkrvJvtwA4wb73KIK', // hashed value from 'unhashedpassword'
  createdAt: new Date('2026-02-18 11:40:00'),
  updatedAt: new Date('2026-02-18 11:40:00'),
};

export default userValid;
