package org.lab.domain.project;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record Ticket(
        UUID id,
        String title,
        String description,
        UUID projectId,
        UUID milestoneId,
        Set<UUID> assigneeIds,
        TicketStatus status
) {
    public Ticket {
        Objects.requireNonNull(id);
        Objects.requireNonNull(title);
        Objects.requireNonNull(projectId);
        Objects.requireNonNull(milestoneId);
        Objects.requireNonNull(status);
        if (title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        description = description == null ? "" : description;
        assigneeIds = assigneeIds == null ? Set.of() : Set.copyOf(assigneeIds);
    }

    public static Ticket create(String title, String description, UUID projectId, UUID milestoneId) {
        return new Ticket(UUID.randomUUID(), title, description, projectId, milestoneId, Set.of(), TicketStatus.NEW);
    }

    public Ticket withAssignee(UUID assigneeId) {
        var newAssignees = new HashSet<>(assigneeIds);
        newAssignees.add(assigneeId);
        return new Ticket(id, title, description, projectId, milestoneId, newAssignees, status);
    }

    public Ticket accept() {
        return new Ticket(id, title, description, projectId, milestoneId, assigneeIds, TicketStatus.ACCEPTED);
    }

    public Ticket startWork() {
        return new Ticket(id, title, description, projectId, milestoneId, assigneeIds, TicketStatus.IN_PROGRESS);
    }

    public Ticket complete() {
        return new Ticket(id, title, description, projectId, milestoneId, assigneeIds, TicketStatus.DONE);
    }

    public boolean isAssignedTo(UUID userId) {
        return assigneeIds.contains(userId);
    }

    public boolean isCompleted() {
        return status == TicketStatus.DONE;
    }
}
