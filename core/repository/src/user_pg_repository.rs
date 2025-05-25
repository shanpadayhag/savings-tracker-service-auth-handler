use dto::repositories::GetUserByEmailWithPasswordRow;
use febiflow::lambda::Result;
use sqlx::PgPool;

#[async_trait::async_trait]
pub trait UserPgRepositoryProvider: Send + Sync {
  async fn get_user_by_email(&self, email: &str) -> Result<GetUserByEmailWithPasswordRow>;
}

pub struct UserPgRepository {
  connection: &'static PgPool,
}

impl UserPgRepository {
  pub fn new(connection: &'static PgPool) -> Self {
    UserPgRepository { connection }
  }
}

#[async_trait::async_trait]
impl UserPgRepositoryProvider for UserPgRepository {
  async fn get_user_by_email(&self, email: &str) -> Result<GetUserByEmailWithPasswordRow> {
    Ok(sqlx::query_as::<_, GetUserByEmailWithPasswordRow>("SELECT * FROM users WHERE email = $1")
      .bind(email)
      .fetch_one(self.connection)
      .await?)
  }
}
