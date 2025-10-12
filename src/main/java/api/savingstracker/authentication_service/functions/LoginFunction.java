package api.savingstracker.authentication_service.functions;

import api.savingstracker.authentication_service.annotations.RequestBody;
import api.savingstracker.authentication_service.aws.ApiGatewayHandler;
import api.savingstracker.authentication_service.requests.LoginRequest;
import api.savingstracker.authentication_service.responses.LoginResponse;

public class LoginFunction extends ApiGatewayHandler {
  public LoginResponse invoke(@RequestBody LoginRequest request) {
    final String email = request.email();
    final String password = request.password();

    if (!email.equals("user@email.com") || !password.equals("7ujb0T4U"))
      return new LoginResponse(false);

    return new LoginResponse(true);
  }
}
