import databaseConfig from 'config/database';
import { Pool } from 'pg';
import { drizzle } from 'drizzle-orm/node-postgres';
import * as schema from '@/infrastructure/database/schema';

const pool = new Pool(databaseConfig);
const db = drizzle(pool, { schema });

export default db;
