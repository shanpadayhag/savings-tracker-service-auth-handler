use dto::requests::LoginRequest;
use ferrum::{
  http::{response::ResponseProvider, Response},
  support::{BodyParser, BodyParserProvider}
};
use lambda_http::{run, service_fn, Error, IntoResponse, Request};
use serde_json::json;
use validator::Validate;

pub async fn function_handler(request: Request) -> Result<impl IntoResponse, Error> {
  let response = Response::new();
  let body = request.body();

  let body_parser = BodyParser::new();
  let login_request_result = body_parser.parse(body);

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

  Ok(response.success(&json!({
    "message": &login_request.email,
  })))
}

#[tokio::main]
async fn main() -> Result<(), Error> {
  run(service_fn(function_handler)).await
}
