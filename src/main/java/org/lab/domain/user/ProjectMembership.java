package org.lab.domain.user;

import java.util.Objects;
import java.util.UUID;

public record ProjectMembership(UUID userId, UUID projectId, Role role) {

    public ProjectMembership {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(projectId);
        Objects.requireNonNull(role);
    }

    public static ProjectMembership of(UUID userId, UUID projectId, Role role) {
        return new ProjectMembership(userId, projectId, role);
    }

    public boolean isForProject(UUID projectId) {
        return this.projectId.equals(projectId);
    }
}
