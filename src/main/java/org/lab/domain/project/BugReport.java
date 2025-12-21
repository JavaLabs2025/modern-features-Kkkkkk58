package org.lab.domain.project;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record BugReport(
        UUID id,
        String title,
        String description,
        UUID projectId,
        UUID reporterId,
        Optional<UUID> assigneeId,
        BugStatus status,
        LocalDateTime createdAt
) {
    public BugReport {
        Objects.requireNonNull(id);
        Objects.requireNonNull(title);
        Objects.requireNonNull(projectId);
        Objects.requireNonNull(reporterId);
        Objects.requireNonNull(assigneeId);
        Objects.requireNonNull(status);
        Objects.requireNonNull(createdAt);
        if (title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }

        description = description == null ? "" : description;
    }

    public static BugReport create(String title, String description, UUID projectId, UUID reporterId) {
        return new BugReport(UUID.randomUUID(), title, description, projectId, reporterId,
                Optional.empty(), BugStatus.NEW, LocalDateTime.now());
    }

    public BugReport withAssignee(UUID assigneeId) {
        return new BugReport(id, title, description, projectId, reporterId, Optional.of(assigneeId), status, createdAt);
    }

    public BugReport markFixed() {
        return new BugReport(id, title, description, projectId, reporterId, assigneeId, BugStatus.FIXED, createdAt);
    }

    public BugReport markTested() {
        return new BugReport(id, title, description, projectId, reporterId, assigneeId, BugStatus.TESTED, createdAt);
    }

    public BugReport close() {
        return new BugReport(id, title, description, projectId, reporterId, assigneeId, BugStatus.CLOSED, createdAt);
    }
}
