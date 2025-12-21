package org.lab.repository;

import java.util.Optional;

import org.lab.domain.user.User;

public class UserRepository extends InMemoryRepository<User> {

    public UserRepository() {
        super(User::id);
    }

    public Optional<User> findByEmail(String email) {
        return findFirst(u -> u.email().equalsIgnoreCase(email));
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
