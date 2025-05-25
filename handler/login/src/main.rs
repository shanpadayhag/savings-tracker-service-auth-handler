use bcrypt::verify;
use dto::requests::LoginRequest;
use febiflow::{
  auth::{Token, TokenProvider}, database, http::{response::ResponseProvider, Response}, support::{BodyParser, BodyParserProvider}
};
use lambda_http::{http::StatusCode, run, service_fn, Error, IntoResponse, Request};
use repository::{UserPgRepository, UserPgRepositoryProvider};
use serde_json::json;
use validator::Validate;
use dotenvy::dotenv;

pub async fn function_handler(request: Request) -> Result<impl IntoResponse, Error> {
  let response = Response::new();
  let body_parser = BodyParser::new();
  let db_pool = database::get_pool().await;
  let user_pg_repository = UserPgRepository::new(db_pool);
  let token = Token::new();

  let login_request_result = body_parser.parse(request.body());

  if let Err(error) = &login_request_result {
    return Ok(response.server_error(&json!({
      "message": error.message,
    })));
  }
  let login_request: LoginRequest = login_request_result.unwrap();

  if let Err(validation_errors) = login_request.validate() {
    return Ok(response.unprocessable_entity(&validation_errors));
  }

  let email = login_request.email.as_ref().unwrap();
  let password = login_request.password.as_ref().unwrap();

  let user = user_pg_repository.get_user_by_email(&email).await;

  if let Err(_) = &user {
    return Ok(response.error_with_status(&json!({
      "message": "Incorrect email or password.",
    }), &StatusCode::UNAUTHORIZED))
  }
  let user = user.unwrap();

  if let Ok(false) | Err(_) = verify(&password, &user.password) {
    return Ok(response.error_with_status(&json!({
      "message": "Incorrect email or password.",
    }), &StatusCode::UNAUTHORIZED))
  }

  let access_token_result = token.encode_token(user.id);

  if let Err(_) = access_token_result {
    return Ok(response.error_with_status(&json!({
      "message": "Failed to generate access token.",
    }), &StatusCode::INTERNAL_SERVER_ERROR))
  }
  let access_token = access_token_result.unwrap();

  Ok(response.success(&json!({
    "email": &email,
    "access_token": &access_token,
  })))
}

#[tokio::main]
async fn main() -> Result<(), Error> {
  dotenv().ok();
  run(service_fn(function_handler)).await
}
