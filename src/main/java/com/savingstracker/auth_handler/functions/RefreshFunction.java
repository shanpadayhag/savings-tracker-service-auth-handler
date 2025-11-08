package com.savingstracker.auth_handler.functions;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.savingstracker.auth_handler.annotations.RequestEvent;
import com.savingstracker.auth_handler.auth.GeneratedTokenDto;
import com.savingstracker.auth_handler.auth.TokenService;
import com.savingstracker.auth_handler.aws.APIGatewayHTTPResponse;
import com.savingstracker.auth_handler.aws.APIGatewayHandler;
import com.savingstracker.auth_handler.entities.RefreshToken;
import com.savingstracker.auth_handler.entities.User;
import com.savingstracker.auth_handler.http.CookieService;
import com.savingstracker.auth_handler.repositories.RefreshTokenRepository;
import com.savingstracker.auth_handler.utils.Hash;

@Component("refresh")
public class RefreshFunction extends APIGatewayHandler {
  private final RefreshTokenRepository refreshTokenRepository;
  private final TokenService tokenService;
  private final CookieService cookieService;

  public RefreshFunction(
      RefreshTokenRepository refreshTokenRepository,
      TokenService tokenService,
      CookieService cookieService) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.tokenService = tokenService;
    this.cookieService = cookieService;
  }

  public APIGatewayV2HTTPResponse invoke(@RequestEvent APIGatewayV2HTTPEvent event) {
    Map<String, String> cookies = event.getCookies().stream()
        .map(cookie -> cookie.split("=", 2))
        .filter(parts -> parts.length == 2)
        .collect(Collectors.toMap(
            parts -> parts[0].trim(),
            parts -> parts[1].trim()));

    String refreshToken = cookies.get("refresh_token");
    String refreshTokenHashed = Hash.hash(refreshToken);
    Instant now = Instant.now();

    Optional<RefreshToken> validRefreshToken = refreshTokenRepository.findValidByTokenHash(
        refreshTokenHashed,
        Instant.now());

    if (validRefreshToken.isEmpty()) {
      return APIGatewayHTTPResponse.status(401)
          .withJsonBody(Map.of("error", "Invalid token."))
          .build();
    }

    final RefreshToken validRefreshTokenValue = validRefreshToken.get();
    final User user = validRefreshTokenValue.getUser();

    final GeneratedTokenDto newRefreshToken = tokenService.generateRefreshToken("email");
    final String newRefreshTokenValue = newRefreshToken.value();
    final Instant newRefreshTokenExpiresAt = newRefreshToken.expiresAt();
    final long newRefreshTokenMaxAge = Duration.between(now, newRefreshTokenExpiresAt).getSeconds();
    final String newRefreshTokenHashed = Hash.hash(newRefreshTokenValue);

    final RefreshToken newRefreshTokenEntity = new RefreshToken();
    newRefreshTokenEntity.setUser(user);
    newRefreshTokenEntity.setTokenHash(newRefreshTokenHashed);
    newRefreshTokenEntity.setExpiresAt(newRefreshTokenExpiresAt);
    refreshTokenRepository.save(newRefreshTokenEntity);
    validRefreshTokenValue.setReplacedByTokenHash(newRefreshTokenHashed);
    validRefreshTokenValue.setRevokedAt(Instant.now());
    refreshTokenRepository.save(validRefreshTokenValue);

    final GeneratedTokenDto accessToken = tokenService.generateAccessToken(user.getEmail());
    final long accessTokenMaxAge = Duration.between(now, accessToken.expiresAt()).getSeconds();

    final String refreshTokenCookie = cookieService.createCookie(
        "refresh_token",
        newRefreshTokenValue,
        newRefreshTokenMaxAge);
    final String accessTokenCookie = cookieService.createCookie(
        "access_token",
        accessToken.value(),
        accessTokenMaxAge);

    return APIGatewayHTTPResponse.ok()
        .withCookies(accessTokenCookie, refreshTokenCookie)
        .withJsonBody(Map.of("status", "Refresh successful."))
        .build();
  }
}
