import z from 'zod';

const envJsonWebToken = z.object({
  JWT_ACCESS_SECRET: z.string(),
  JWT_ACCESS_EXPIRES_IN: z.string().optional().default('15m'),
  JWT_REFRESH_SECRET: z.string(),
  JWT_REFRESH_EXPIRES_IN: z.string().optional().default('7d'),
});

const env = envJsonWebToken.parse(process.env);

const jsonWebToken = {
  ACCESS_SECRET: env.JWT_ACCESS_SECRET,
  ACCESS_EXPIRES_IN: env.JWT_ACCESS_EXPIRES_IN,
  REFRESH_SECRET: env.JWT_REFRESH_SECRET,
  REFRESH_EXPIRES_IN: env.JWT_REFRESH_EXPIRES_IN,
};

export default jsonWebToken;
