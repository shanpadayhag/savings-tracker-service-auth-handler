package com.savingstracker.auth_handler.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.savingstracker.auth_handler.entities.User;
import com.savingstracker.auth_handler.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {
  private final UserRepository repository;

  public UserServiceImpl(UserRepository repository) {
    this.repository = repository;
  }

  public Optional<User> findByEmail(String email) {
    return repository.findByEmail(email);
  }
}
