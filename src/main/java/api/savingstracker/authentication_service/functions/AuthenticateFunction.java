package api.savingstracker.authentication_service.functions;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import api.savingstracker.authentication_service.annotations.RequestEvent;
import api.savingstracker.authentication_service.auth.TokenService;
import api.savingstracker.authentication_service.aws.ApiGatewayEvent;
import api.savingstracker.authentication_service.aws.ApiGatewayHandler;
import api.savingstracker.authentication_service.aws.ApiResponse;
import api.savingstracker.authentication_service.aws.ApiResponseBuilder;
import api.savingstracker.authentication_service.http.CookieService;

public class AuthenticateFunction extends ApiGatewayHandler {
  private final TokenService tokenService = new TokenService();
  private final CookieService cookieService = new CookieService();

  public ApiResponse invoke(@RequestEvent ApiGatewayEvent event) {
    ApiResponseBuilder responseBuilder = new ApiResponseBuilder();

    Map<String, String> cookies = event.getCookiesAsMap();

    if (cookies == null || cookies.isEmpty()) {
      return responseBuilder
          .withStatusCode(401)
          .withJsonBody(Map.of("error", "Unauthorized: Refresh token not found."))
          .build();
    }

    String refreshToken = cookies.get("refresh_token");

    if (refreshToken == null || refreshToken.isEmpty()) {
      return responseBuilder
          .withStatusCode(401)
          .withJsonBody(Map.of("error", "Unauthorized: Refresh token not found."))
          .build();
    }

    Optional<String> validatedUserId = tokenService.validateToken(refreshToken);

    if (validatedUserId.isEmpty()) {
      return responseBuilder
          .withStatusCode(401)
          .withJsonBody(Map.of("error", "Unauthorized: Invalid token."))
          .build();
    }

    String accessToken = tokenService.generateAccessToken(validatedUserId.toString());
    String accessTokenCookie = cookieService.createCookie("access_token", accessToken, TimeUnit.MINUTES.toSeconds(15));

    return responseBuilder
        .withStatusCode(200)
        .withCookie(accessTokenCookie)
        .withJsonBody(Map.of("authenticated", true))
        .build();
  }
}
