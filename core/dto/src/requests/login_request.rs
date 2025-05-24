use serde::Deserialize;
use validator::Validate;

#[derive(Debug, Deserialize, Validate)]
pub struct LoginRequest {
  #[validate(
    required(message = "Oops! We need your email address to proceed."),
    email(message="Oops! It looks like that might not be a valid email address."),
    length(max=255, message="Whoa, that's a long email! Our system only allows emails up to 255 characters. Could you shorten it a bit?")
  )]
  pub email: Option<String>,

  #[validate(
    required(message = "Please enter your password. Passwords can be tricky, but you've got this!"),
    length(max=255, message="That's a super secure password! But it's a tad too long for our system. Could you trim it down to 255 characters or less?"),
  )]
  pub password: Option<String>,
}
