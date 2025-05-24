use dotenv::dotenv;
use std::env;

pub trait IEnv {
  fn get_app_key(&self) -> String;
  fn get_database_url(&self) -> String;
  fn get_allowed_origins(&self) -> Vec<String>;
}

pub struct Env {
  app_key: String,
  database_url: String,
  allowed_origins: Vec<String>,
}

impl Env {
  pub fn new() -> Self {
    dotenv().ok();

    let app_key = env::var("APP_KEY")
      .expect("APP_KEY must be set in the environment");
    let database_url = env::var("DB_URL")
      .expect("DB_URL must be set in the environment");
    let allowed_origins: Vec<String> = env::var("APP_ALLOWED_ORIGINS")
      .unwrap_or_default()
      .split(',')
      .map(|s| s.trim().to_string())
      .filter(|s| !s.is_empty())
      .collect();

    Self { app_key, database_url, allowed_origins }
  }
}

impl IEnv for Env {
  fn get_app_key(&self) -> String {
    self.app_key.clone()
  }

  fn get_database_url(&self) -> String {
    self.database_url.clone()
  }

  fn get_allowed_origins(&self) -> Vec<String> {
    self.allowed_origins.clone()
  }
}

#[cfg(test)]
mod tests {
  use super::*;
  use std::env;

  #[test]
  fn test_env_creation() {
    // arrange
    unsafe {
      env::set_var("APP_KEY", "test_app_key");
      env::set_var("DB_URL", "test_database_url");
    }

    // act
    let env_instance = Env::new();

    // assert
    assert_eq!(env_instance.get_app_key(), "test_app_key");
    assert_eq!(env_instance.get_database_url(), "test_database_url");

    unsafe {
      env::remove_var("APP_KEY");
      env::remove_var("DB_URL");
    }
  }
}
