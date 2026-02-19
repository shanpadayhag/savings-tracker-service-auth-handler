import { z } from "zod";

const envDatabase = z.object({
  DATABASE_URL: z.string(),
});

const env = envDatabase.parse(process.env);

const database = {
  url: env.DATABASE_URL,
};

export default database;
