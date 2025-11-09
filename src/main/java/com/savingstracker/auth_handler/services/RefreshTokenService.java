package com.savingstracker.auth_handler.services;

import java.time.Instant;

import com.savingstracker.auth_handler.entities.RefreshToken;
import com.savingstracker.auth_handler.entities.User;

public interface RefreshTokenService {
  public void insert(User user, String hashedValue, Instant expiresAt);

  public void rotateTokens(RefreshToken oldToken, RefreshToken newToken);
}
