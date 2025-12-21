package org.lab.service;

import java.util.Optional;
import java.util.UUID;

import org.lab.domain.common.Result;
import org.lab.domain.user.User;
import org.lab.repository.UserRepository;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Result<User> register(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            return Result.failure("User with email " + email + " already exists");
        }
        try {
            var user = User.create(name, email);
            userRepository.save(user);
            return Result.success(user);
        } catch (IllegalArgumentException e) {
            return Result.failure(e.getMessage());
        }
    }

    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }
}
