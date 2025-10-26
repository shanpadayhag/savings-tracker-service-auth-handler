package com.savingstracker.auth_handler.functions;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.savingstracker.auth_handler.annotations.RequestBody;
import com.savingstracker.auth_handler.auth.TokenService;
import com.savingstracker.auth_handler.aws.APIGatewayHTTPResponse;
import com.savingstracker.auth_handler.aws.APIGatewayHandler;
import com.savingstracker.auth_handler.dtos.requests.LoginRequest;
import com.savingstracker.auth_handler.entities.User;
import com.savingstracker.auth_handler.http.CookieService;
import com.savingstracker.auth_handler.repositories.UserRepository;

@Component("login")
public class LoginFunction extends APIGatewayHandler {
  private final TokenService tokenService;
  private final CookieService cookieService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public LoginFunction(
      TokenService tokenService,
      CookieService cookieService,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    this.tokenService = tokenService;
    this.cookieService = cookieService;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public APIGatewayV2HTTPResponse invoke(@RequestBody LoginRequest request) {
    try {
      final String email = request.email();
      final String password = request.password();

      System.out.println("password: " + password);
      System.out.println("encoded password: " + passwordEncoder.encode(password));

      final User user = userRepository.findByEmail(email);

      if (user == null || !passwordEncoder.matches(password, user.getPassword()))
        throw new InvalidCredentialsException();

      final String accessToken = tokenService.generateAccessToken(email);
      final String refreshToken = tokenService.generateRefreshToken(email);

      final String accessTokenCookie = cookieService.createCookie("access_token", accessToken,
          TimeUnit.MINUTES.toSeconds(15));
      final String refreshTokenCookie = cookieService.createCookie("refresh_token", refreshToken,
          TimeUnit.DAYS.toSeconds(7));

      return APIGatewayHTTPResponse.ok()
          .withCookies(accessTokenCookie, refreshTokenCookie)
          .withJsonBody(Map.of("status", "Login successful."))
          .build();
    } catch (InvalidCredentialsException exception) {
      return APIGatewayHTTPResponse.status(401)
          .withJsonBody(Map.of("error", "Invalid credentials."))
          .build();
    } catch (Exception exception) {
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
