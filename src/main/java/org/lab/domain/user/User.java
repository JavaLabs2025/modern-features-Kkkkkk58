package org.lab.domain.user;

import java.util.Objects;
import java.util.UUID;

public record User(UUID id, String name, String email) {

    public User {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(email);
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
    }

    public static User create(String name, String email) {
        return new User(UUID.randomUUID(), name, email);
    }
}
