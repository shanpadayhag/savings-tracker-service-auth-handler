import database from 'config/database';
import { drizzle } from 'drizzle-orm/postgres-js';
import postgres from 'postgres';

let connectionString = database.url;
if (connectionString.includes('postgres:postgres@supabase_db_')) {
  const url = new URL(connectionString);
  url.hostname = url.hostname.split('_')[1] || '';
  connectionString = url.href;
}

export const client = postgres(connectionString, { prepare: false });
const db = drizzle(client);

export default db;
