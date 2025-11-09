package com.savingstracker.auth_handler.services;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.savingstracker.auth_handler.entities.RefreshToken;
import com.savingstracker.auth_handler.entities.User;
import com.savingstracker.auth_handler.repositories.RefreshTokenRepository;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
  private final RefreshTokenRepository repository;

  public RefreshTokenServiceImpl(RefreshTokenRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void insert(User user, String hashedValue, Instant expiresAt) {
    final RefreshToken refreshTokenEntity = new RefreshToken();
    refreshTokenEntity.setUser(user);
    refreshTokenEntity.setTokenHash(hashedValue);
    refreshTokenEntity.setExpiresAt(expiresAt);
    repository.save(refreshTokenEntity);
  }

  @Transactional
  public void rotateTokens(RefreshToken oldToken, RefreshToken newToken) {
    repository.save(newToken);

    oldToken.setReplacedByTokenHash(newToken.getTokenHash());
    oldToken.setRevokedAt(Instant.now());
    repository.save(oldToken);
  }
}
