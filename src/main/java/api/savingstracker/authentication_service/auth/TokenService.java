package api.savingstracker.authentication_service.auth;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

public class TokenService {
  private static final String JWT_SECRET = "your-super-secret-key-that-is-long-and-random";
  private static final Algorithm ALGORITHM = Algorithm.HMAC256(JWT_SECRET);
  private static final String ISSUER = "savings-tracker-api";
  private static final JWTVerifier VERIFIER = JWT.require(ALGORITHM)
      .withIssuer(ISSUER)
      .build();

  public String generateAccessToken(String value) {
    return JWT.create()
        .withIssuer(ISSUER)
        .withSubject(value)
        .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)))
        .sign(ALGORITHM);
  }

  public String generateRefreshToken(String value) {
    return JWT.create()
        .withIssuer(ISSUER)
        .withSubject(value)
        .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)))
        .sign(ALGORITHM);
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
