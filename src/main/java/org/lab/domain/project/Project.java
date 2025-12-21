package org.lab.domain.project;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record Project(
        UUID id,
        String name,
        String description,
        UUID managerId,
        Optional<UUID> teamLeadId,
        Set<UUID> developerIds,
        Set<UUID> testerIds
) {
    public Project {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(managerId);
        Objects.requireNonNull(teamLeadId);
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        description = description == null ? "" : description;
        developerIds = developerIds == null ? Set.of() : Set.copyOf(developerIds);
        testerIds = testerIds == null ? Set.of() : Set.copyOf(testerIds);
    }

    public static Project create(String name, String description, UUID managerId) {
        return new Project(UUID.randomUUID(), name, description, managerId, Optional.empty(), Set.of(), Set.of());
    }

    public Project withTeamLead(UUID teamLeadId) {
        return new Project(id, name, description, managerId, Optional.of(teamLeadId), developerIds, testerIds);
    }

    public Project withDeveloper(UUID developerId) {
        var newDevs = new HashSet<>(developerIds);
        newDevs.add(developerId);
        return new Project(id, name, description, managerId, teamLeadId, newDevs, testerIds);
    }

    public Project withTester(UUID testerId) {
        var newTesters = new HashSet<>(testerIds);
        newTesters.add(testerId);
        return new Project(id, name, description, managerId, teamLeadId, developerIds, newTesters);
    }

    public boolean isDeveloper(UUID userId) {
        return developerIds.contains(userId) || teamLeadId.map(id -> id.equals(userId)).orElse(false);
    }
}
