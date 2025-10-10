package api.savingstracker.authentication_service.functions;

import org.springframework.stereotype.Component;
import api.savingstracker.authentication_service.annotations.RequestBody;
import api.savingstracker.authentication_service.aws.ApiGatewayHandler;
import api.savingstracker.authentication_service.requests.LoginRequest;
import api.savingstracker.authentication_service.responses.LoginResponse;

@Component
public class LoginFunction extends ApiGatewayHandler {
  public LoginResponse invoke(@RequestBody LoginRequest request) {
    final String email = request.email();
    final String password = request.password();

    if (email != "user@email.com" || password != "7ujb0T4U")
      return new LoginResponse(false);

    return new LoginResponse(true);
  }
}
