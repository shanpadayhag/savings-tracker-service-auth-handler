package com.savingstracker.auth_handler.services;

import java.util.Optional;

import com.savingstracker.auth_handler.entities.User;

public interface UserService {
  public Optional<User> findByEmail(String email);
}
