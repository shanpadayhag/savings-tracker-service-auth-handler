#[derive(Debug, Clone)]
pub enum ErrorKind {
  RequestPayloadInvalid,
  ValidationFailed,
}

pub struct Error {
  pub kind: ErrorKind,
  pub message: String,
}
