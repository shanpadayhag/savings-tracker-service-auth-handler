package com.savingstracker.auth_handler.functions;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.savingstracker.auth_handler.annotations.RequestBody;
import com.savingstracker.auth_handler.auth.GeneratedTokenDto;
import com.savingstracker.auth_handler.auth.TokenService;
import com.savingstracker.auth_handler.aws.APIGatewayHTTPResponse;
import com.savingstracker.auth_handler.aws.APIGatewayHandler;
import com.savingstracker.auth_handler.dtos.requests.LoginRequest;
import com.savingstracker.auth_handler.entities.User;
import com.savingstracker.auth_handler.http.CookieService;
import com.savingstracker.auth_handler.services.RefreshTokenService;
import com.savingstracker.auth_handler.services.UserService;
import com.savingstracker.auth_handler.utils.Hash;

@Component("login")
public class LoginFunction extends APIGatewayHandler {
  private final TokenService tokenService;
  private final CookieService cookieService;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenService refreshTokenService;
  private final UserService service;

  public LoginFunction(
      TokenService tokenService,
      CookieService cookieService,
      PasswordEncoder passwordEncoder,
      RefreshTokenService refreshTokenService,
      UserService service) {
    this.tokenService = tokenService;
    this.cookieService = cookieService;
    this.passwordEncoder = passwordEncoder;
    this.refreshTokenService = refreshTokenService;
    this.service = service;
  }

  public APIGatewayV2HTTPResponse invoke(@RequestBody LoginRequest request) {
    try {
      final Instant now = Instant.now();
      final String email = request.email();
      final String password = request.password();

      final Optional<User> optionalUser = service.findByEmail(email);
      final User user = optionalUser.get();

      if (optionalUser.isEmpty() || !passwordEncoder.matches(password, user.getPassword()))
        throw new InvalidCredentialsException();

      final GeneratedTokenDto refreshToken = tokenService.generateRefreshToken(email);
      final String refreshTokenValue = refreshToken.value();
      final Instant refreshTokenExpiresAt = refreshToken.expiresAt();
      final long refreshTokenMaxAge = Duration.between(now, refreshTokenExpiresAt).getSeconds();
      final String refreshTokenHashed = Hash.hash(refreshTokenValue);
      refreshTokenService.insert(user, refreshTokenHashed, refreshTokenExpiresAt);

      final GeneratedTokenDto accessToken = tokenService.generateAccessToken(email);
      final long accessTokenMaxAge = Duration.between(now, accessToken.expiresAt()).getSeconds();

      final String refreshTokenCookie = cookieService.createCookie(
          "refresh_token",
          refreshTokenValue,
          refreshTokenMaxAge);
      final String accessTokenCookie = cookieService.createCookie(
          "access_token",
          accessToken.value(),
          accessTokenMaxAge);

      return APIGatewayHTTPResponse.ok()
          .withCookies(accessTokenCookie, refreshTokenCookie)
          .withJsonBody(Map.of("status", "Login successful."))
          .build();
    } catch (InvalidCredentialsException exception) {
      return APIGatewayHTTPResponse.status(401)
          .withJsonBody(Map.of("error", "Invalid credentials."))
          .build();
    } catch (Exception exception) {
      System.out.println("Login Failed Because: " + exception.getMessage());
      return APIGatewayHTTPResponse.status(500)
          .withJsonBody(Map.of("error", "Internal Server Error."))
          .build();
    }
  }

  private static class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException() {
      super("Invalid credentials.");
    }
  }
}
