import 'dotenv/config'
import createApp from '@/app';
import serverless from 'serverless-http';

const app = createApp();

if (process.env.NODE_ENV !== "production") {
  const PORT = process.env.PORT || 8000;

  app.listen(PORT, () => {
    console.log(`🚀 Server running on http://localhost:${PORT}`);
  });
}

export const handler = serverless(app);
