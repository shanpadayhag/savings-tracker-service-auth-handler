package com.savingstracker.auth_handler.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Service
public class TokenService {
  private static final String JWT_SECRET = "your-super-secret-key-that-is-long-and-random";
  private static final Algorithm ALGORITHM = Algorithm.HMAC256(JWT_SECRET);
  private static final String ISSUER = "savings-tracker-api";
  private static final JWTVerifier VERIFIER = JWT.require(ALGORITHM)
      .withIssuer(ISSUER)
      .build();

  public GeneratedTokenDto generateAccessToken(String value) {
    final Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);

    String token =  JWT.create()
        .withIssuer(ISSUER)
        .withSubject(value)
        .withExpiresAt(expiresAt)
        .sign(ALGORITHM);

    return new GeneratedTokenDto(token, expiresAt);
  }

  public GeneratedTokenDto generateRefreshToken(String value) {
    final Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

    String token = JWT.create()
        .withIssuer(ISSUER)
        .withSubject(value)
        .withExpiresAt(expiresAt)
        .sign(ALGORITHM);

    return new GeneratedTokenDto(token, expiresAt);
  }

  public Optional<String> validateToken(String token) {
    try {
      DecodedJWT decodedJWT = VERIFIER.verify(token);
      return Optional.of(decodedJWT.getSubject());
    } catch (JWTVerificationException exception) {
      return Optional.empty();
    }
  }
}
