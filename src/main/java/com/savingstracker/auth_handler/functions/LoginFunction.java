package com.savingstracker.auth_handler.functions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.savingstracker.auth_handler.annotations.RequestBody;
import com.savingstracker.auth_handler.auth.TokenService;
import com.savingstracker.auth_handler.aws.APIGatewayHandler;
import com.savingstracker.auth_handler.dtos.requests.LoginRequest;
import com.savingstracker.auth_handler.http.CookieService;

@Component("login")
public class LoginFunction extends APIGatewayHandler {
  private TokenService tokenService;
  private CookieService cookieService;

  public LoginFunction(
      TokenService tokenService,
      CookieService cookieService) {
    this.tokenService = tokenService;
    this.cookieService = cookieService;
  }

  public APIGatewayV2HTTPResponse invoke(@RequestBody LoginRequest request) {
    try {
      authenticate(request.email(), request.password());

      String accessToken = tokenService.generateAccessToken(request.email());
      String refreshToken = tokenService.generateRefreshToken(request.email());

      String accessTokenCookie = cookieService.createCookie("access_token", accessToken, TimeUnit.MINUTES.toSeconds(15));
      String refreshTokenCookie = cookieService.createCookie("refresh_token", refreshToken, TimeUnit.DAYS.toSeconds(7));

      return APIGatewayV2HTTPResponse.builder()
          .withStatusCode(200)
          .withCookies(List.of(accessTokenCookie, refreshTokenCookie))
          .withBody(Map.of("status", "Login successful.").toString())
          .build();
    } catch (InvalidCredentialsException exception) {
      return APIGatewayV2HTTPResponse.builder()
          .withStatusCode(401)
          .withBody(Map.of("error", exception.getMessage()).toString())
          .build();
    } catch (Exception exception) {
      return APIGatewayV2HTTPResponse.builder()
          .withStatusCode(500)
          .withBody(Map.of("error", "Internal Server Error.").toString())
          .build();
    }
  }

  private void authenticate(String email, String password) throws InvalidCredentialsException {
    if (!"user@email.com".equals(email) || !"7ujb0T4U".equals(password)) {
      throw new InvalidCredentialsException();
    }
  }

  private static class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException() {
      super("Invalid credentials");
    }
  }
}
