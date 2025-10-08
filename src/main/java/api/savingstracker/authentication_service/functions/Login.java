package api.savingstracker.authentication_service.functions;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import api.savingstracker.authentication_service.requests.LoginRequest;
import api.savingstracker.authentication_service.responses.LoginResponse;

@Component
public class Login implements Function<LoginRequest, LoginResponse> {
  @Override
  public LoginResponse apply(LoginRequest request) {
    return new LoginResponse("dummy-token-for-" + request.email());
  }
}
