package org.lab.repository;

import java.util.Optional;
import java.util.UUID;

import org.lab.domain.project.Milestone;
import org.lab.domain.project.MilestoneStatus;

public class MilestoneRepository extends InMemoryRepository<Milestone> {

    public MilestoneRepository() {
        super(Milestone::id);
    }

    public Optional<Milestone> findActiveByProjectId(UUID projectId) {
        return findFirst(m -> m.projectId().equals(projectId) && m.status() == MilestoneStatus.ACTIVE);
    }

    public boolean hasActiveMilestone(UUID projectId) {
        return findActiveByProjectId(projectId).isPresent();
    }
}
