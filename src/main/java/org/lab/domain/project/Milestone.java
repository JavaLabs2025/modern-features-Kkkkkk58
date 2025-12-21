package org.lab.domain.project;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record Milestone(
        UUID id,
        String name,
        UUID projectId,
        LocalDate startDate,
        LocalDate endDate,
        MilestoneStatus status
) {
    public Milestone {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(projectId);
        Objects.requireNonNull(startDate);
        Objects.requireNonNull(endDate);
        Objects.requireNonNull(status);
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date before start");
        }
    }

    public static Milestone create(String name, UUID projectId, LocalDate startDate, LocalDate endDate) {
        return new Milestone(UUID.randomUUID(), name, projectId, startDate, endDate, MilestoneStatus.OPEN);
    }

    public Milestone activate() {
        return new Milestone(id, name, projectId, startDate, endDate, MilestoneStatus.ACTIVE);
    }

    public Milestone close() {
        return new Milestone(id, name, projectId, startDate, endDate, MilestoneStatus.CLOSED);
    }

    public boolean canAddTickets() {
        return status != MilestoneStatus.CLOSED;
    }
}
