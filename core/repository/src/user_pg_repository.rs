pub trait UserPgRepositoryProvider {
  fn get_user_by_email(&self, email: &str);
}

pub struct UserPgRepository {}

impl UserPgRepository {
  pub fn new() -> Self { UserPgRepository {} }
}

impl UserPgRepositoryProvider for UserPgRepository {
  fn get_user_by_email(&self, email: &str) {}
}
