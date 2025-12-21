package org.lab.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.lab.domain.user.ProjectMembership;
import org.lab.domain.user.Role;

public class MembershipRepository {

    private final Map<String, ProjectMembership> storage = new ConcurrentHashMap<>();

    private static String key(UUID userId, UUID projectId) {
        return userId + ":" + projectId;
    }

    public ProjectMembership save(ProjectMembership membership) {
        storage.put(key(membership.userId(), membership.projectId()), membership);
        return membership;
    }

    public Optional<ProjectMembership> find(UUID userId, UUID projectId) {
        return Optional.ofNullable(storage.get(key(userId, projectId)));
    }

    public Optional<Role> getRole(UUID userId, UUID projectId) {
        return find(userId, projectId).map(ProjectMembership::role);
    }

    public List<ProjectMembership> findByProjectId(UUID projectId) {
        return storage.values().stream()
                .filter(m -> m.isForProject(projectId))
                .toList();
    }
}
