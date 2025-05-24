use ferrum::http::{Response, response::IResponse};
use lambda_http::{Error, IntoResponse, Request, run, service_fn};
use serde_json::json;

pub async fn function_handler(_request: Request) -> Result<impl IntoResponse, Error> {
  let response = Response::new();

  Ok(response.success(&json!({
    "message": "Hello world!"
  })))
}

#[tokio::main]
async fn main() -> Result<(), Error> {
  run(service_fn(function_handler)).await
}
