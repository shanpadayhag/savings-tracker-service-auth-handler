package api.savingstracker.authentication_service.functions;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import api.savingstracker.authentication_service.annotations.RequestBody;
import api.savingstracker.authentication_service.auth.TokenService;
import api.savingstracker.authentication_service.aws.ApiGatewayHandler;
import api.savingstracker.authentication_service.aws.ApiResponse;
import api.savingstracker.authentication_service.aws.ApiResponseBuilder;
import api.savingstracker.authentication_service.http.CookieService;
import api.savingstracker.authentication_service.requests.LoginRequest;

public class LoginFunction extends ApiGatewayHandler {
    private final TokenService tokenService;
    private final CookieService cookieService;

    public LoginFunction() {
        this.tokenService = new TokenService();
        this.cookieService = new CookieService();
    }

    public ApiResponse invoke(@RequestBody LoginRequest request) {
        ApiResponseBuilder responseBuilder = new ApiResponseBuilder();
        try {
            authenticate(request.email(), request.password());

            String accessToken = tokenService.generateAccessToken(request.email());
            String refreshToken = tokenService.generateRefreshToken(request.email());

            String accessTokenCookie = cookieService.createCookie("access_token", accessToken, TimeUnit.MINUTES.toSeconds(15));
            String refreshTokenCookie = cookieService.createCookie("refresh_token", refreshToken, TimeUnit.DAYS.toSeconds(7));

            return responseBuilder
                .withStatusCode(200)
                .withCookie(accessTokenCookie)
                .withCookie(refreshTokenCookie)
                .withJsonBody(Map.of("status", "Login successful"))
                .build();

        } catch (InvalidCredentialsException e) {
            return responseBuilder
                .withStatusCode(401)
                .withJsonBody(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            return responseBuilder
                .withStatusCode(500)
                .withJsonBody(Map.of("error", "Internal Server Error."))
                .build();
        }
    }

    private void authenticate(String email, String password) throws InvalidCredentialsException {
        if (!"user@email.com".equals(email) || !"7ujb0T4U".equals(password)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }

    private static class InvalidCredentialsException extends Exception {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }
}
