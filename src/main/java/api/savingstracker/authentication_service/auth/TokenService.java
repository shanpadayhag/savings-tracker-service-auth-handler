package api.savingstracker.authentication_service.auth;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Service
public class TokenService {
  private static final String JWT_SECRET = "your-super-secret-key-that-is-long-and-random";
  private static final Algorithm ALGORITHM = Algorithm.HMAC256(JWT_SECRET);
  private static final String ISSUER = "savings-tracker-api";

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
}
