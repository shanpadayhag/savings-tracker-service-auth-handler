use sqlx::FromRow;
use uuid::Uuid;

#[derive(Debug, FromRow)]
pub struct GetUserByEmailWithPasswordRow {
  pub id: Uuid,
  pub email: String,
  pub password: String,
}
