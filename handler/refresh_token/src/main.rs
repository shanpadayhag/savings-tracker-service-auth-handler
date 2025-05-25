use lambda_http::{
  http::{StatusCode, Response},
  Error, IntoResponse, Request, RequestPayloadExt,
};
use serde::{Deserialize, Serialize};
use serde_json::json;
use std::fmt;

// Your application-specific error enum
#[derive(Debug, Clone)]
pub enum AppError {
  RequestPayloadInvalid,
  UsernameNotFound,
  AnotherCustomErrorThatOnlyInMyApp,
}

impl fmt::Display for AppError {
  fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
      match self {
          AppError::RequestPayloadInvalid => write!(f, "Invalid request payload"),
          AppError::UsernameNotFound => write!(f, "Username not found"),
          AppError::AnotherCustomErrorThatOnlyInMyApp => write!(f, "Custom application error"),
      }
  }
}

impl std::error::Error for AppError {}

// Configuration for error responses
pub struct ErrorConfig {
  pub status_code: StatusCode,
  pub message: String,
}

impl AppError {
  pub fn to_error_config(&self) -> ErrorConfig {
      match self {
          AppError::RequestPayloadInvalid => ErrorConfig {
              status_code: StatusCode::BAD_REQUEST,
              message: "Invalid request payload".to_string(),
          },
          AppError::UsernameNotFound => ErrorConfig {
              status_code: StatusCode::NOT_FOUND,
              message: "Username not found".to_string(),
          },
          AppError::AnotherCustomErrorThatOnlyInMyApp => ErrorConfig {
              status_code: StatusCode::INTERNAL_SERVER_ERROR,
              message: "An application error occurred".to_string(),
          },
      }
  }
}

// Wrapper result type for cleaner error handling
pub type AppResult<T> = Result<T, AppError>;

// Trait for types that can be converted to HTTP responses
pub trait IntoHttpResponse {
  fn into_response(self) -> Result<Response<String>, Error>;
}

// Implementation for serializable types
impl<T> IntoHttpResponse for T
where
  T: Serialize,
{
  fn into_response(self) -> Result<Response<String>, Error> {
      let body = serde_json::to_string(&self)
          .map_err(|e| Error::from(format!("Serialization error: {}", e)))?;

      Response::builder()
          .status(StatusCode::OK)
          .header("Content-Type", "application/json")
          .body(body)
          .map_err(|e| Error::from(format!("Response building error: {}", e)))
  }
}

// Main wrapper function
pub async fn handle_request<F, Fut, T, P>(
  event: Request,
  handler: F,
) -> Result<impl IntoResponse, Error>
where
  F: FnOnce(P) -> Fut,
  Fut: std::future::Future<Output = AppResult<T>>,
  T: IntoHttpResponse,
  P: for<'de> Deserialize<'de>,
{
  // Parse the payload
  let payload = match event.payload::<P>() {
      Ok(Some(p)) => p,
      Ok(None) => {
          return create_error_response(AppError::RequestPayloadInvalid);
      }
      Err(_) => {
          return create_error_response(AppError::RequestPayloadInvalid);
      }
  };

  // Call the handler
  match handler(payload).await {
      Ok(response_data) => response_data.into_response(),
      Err(app_error) => create_error_response(app_error),
  }
}

// Helper function to create error responses
fn create_error_response(error: AppError) -> Result<Response<String>, Error> {
  let config = error.to_error_config();
  let body = json!({
      "error": config.message,
      "status": config.status_code.as_u16()
  })
  .to_string();

  Response::builder()
      .status(config.status_code)
      .header("Content-Type", "application/json")
      .body(body)
      .map_err(|e| Error::from(format!("Error response building error: {}", e)))
}

// Convenience macro for creating handlers
#[macro_export]
macro_rules! lambda_handler {
  ($handler:expr) => {
      |event: Request| async move { handle_request(event, $handler).await }
  };
}

// Example usage:

use lambda_http::{run, service_fn};

#[tokio::main]
async fn main() -> Result<(), Error> {
  run(service_fn(lambda_handler!(my_business_logic))).await
}

// Your clean business logic function
async fn my_business_logic(payload: MyPayload) -> AppResult<MyResponse> {
  // Simulate some validation
  if payload.prop1.is_empty() {
      return Err(AppError::RequestPayloadInvalid);
  }

  // Simulate username lookup
  if payload.prop2 == "nonexistent" {
      return Err(AppError::UsernameNotFound);
  }

  // Simulate another business logic error
  if payload.prop1 == "trigger_error" {
      return Err(AppError::AnotherCustomErrorThatOnlyInMyApp);
  }

  // Success case - return your response struct
  Ok(MyResponse {
      message: "Success!".to_string(),
      processed_data: format!("Processed: {} - {}", payload.prop1, payload.prop2),
      timestamp: chrono::Utc::now().to_rfc3339(),
  })
}

// Alternative usage without macro
pub async fn function_handler_alternative(event: Request) -> Result<impl IntoResponse, Error> {
  handle_request(event, my_business_logic).await
}

#[derive(Deserialize, Serialize, Debug, Clone)]
pub struct MyPayload {
  pub prop1: String,
  pub prop2: String,
}

#[derive(Serialize, Debug)]
pub struct MyResponse {
  pub message: String,
  pub processed_data: String,
  pub timestamp: String,
}
