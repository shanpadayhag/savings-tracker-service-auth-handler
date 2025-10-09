package api.savingstracker.authentication_service.functions;

import org.springframework.stereotype.Component;
import api.savingstracker.authentication_service.annotations.RequestBody;
import api.savingstracker.authentication_service.aws.ApiGatewayHandler;
import api.savingstracker.authentication_service.requests.LoginRequest;
import api.savingstracker.authentication_service.responses.LoginResponse;

@Component
public class Login extends ApiGatewayHandler {
    public LoginResponse invoke(@RequestBody LoginRequest request) {
        return new LoginResponse("dummy-token-for-" + request.email());
    }
}
