import * as schema from '@/infrastructure/database/schema';

const newUserValid: schema.NewUser = {
  firstName: 'John',
  lastName: 'Doe',
  email: 'john@example.com',
  password: 'hashed',
};

export default newUserValid;
