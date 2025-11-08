package com.savingstracker.auth_handler.repositories;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.savingstracker.auth_handler.entities.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user u WHERE rt.tokenHash = :tokenHash AND rt.revokedAt IS NULL AND rt.expiresAt > :currentTime")
  Optional<RefreshToken> findValidByTokenHash(@Param("tokenHash") String tokenHash,
      @Param("currentTime") Instant currentTime);
}
