package org.lab.service;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Function;

import org.lab.domain.common.Result;
import org.lab.domain.project.Milestone;
import org.lab.domain.project.MilestoneStatus;
import org.lab.domain.user.Manager;
import org.lab.repository.MembershipRepository;
import org.lab.repository.MilestoneRepository;
import org.lab.repository.ProjectRepository;
import org.lab.repository.TicketRepository;

public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final TicketRepository ticketRepository;
    private final MembershipRepository membershipRepository;

    public MilestoneService(
            MilestoneRepository milestoneRepository,
            ProjectRepository projectRepository,
            TicketRepository ticketRepository,
            MembershipRepository membershipRepository
    ) {
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
        this.ticketRepository = ticketRepository;
        this.membershipRepository = membershipRepository;
    }

    public Result<Milestone> createMilestone(String name, UUID projectId, LocalDate start, LocalDate end, UUID userId) {
        if (!isManager(userId, projectId)) {
            return Result.failure("User cannot create milestone");
        }
        if (projectRepository.findById(projectId).isEmpty()) {
            return Result.failure("Project is not found");
        }
        try {
            var milestone = Milestone.create(name, projectId, start, end);
            milestoneRepository.save(milestone);
            return Result.success(milestone);
        } catch (IllegalArgumentException e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Milestone> activateMilestone(UUID milestoneId, UUID userId) {
        return withMilestone(milestoneId, userId, milestone -> {
            if (milestoneRepository.hasActiveMilestone(milestone.projectId())) {
                return Result.failure("Project already has an active milestone");
            }
            if (milestone.status() != MilestoneStatus.OPEN) {
                return Result.failure("Can activate only open milestone");
            }
            var activated = milestone.activate();
            milestoneRepository.save(activated);
            return Result.success(activated);
        });
    }

    public Result<Milestone> closeMilestone(UUID milestoneId, UUID userId) {
        return withMilestone(milestoneId, userId, milestone -> {
            if (milestone.status() != MilestoneStatus.ACTIVE) {
                return Result.failure("Can close only active milestone");
            }
            long incomplete = ticketRepository.countIncomplete(milestoneId);
            if (incomplete > 0) {
                return Result.failure("Cannot close milestone: " + incomplete + " incomplete tickets found");
            }
            var closed = milestone.close();
            milestoneRepository.save(closed);
            return Result.success(closed);
        });
    }

    private boolean isManager(UUID userId, UUID projectId) {
        return membershipRepository.getRole(userId, projectId)
                .map(r -> r instanceof Manager).orElse(false);
    }

    private Result<Milestone> withMilestone(
            UUID id,
            UUID userId,
            Function<Milestone, Result<Milestone>> action
    ) {
        var milestone = milestoneRepository.findById(id);
        if (milestone.isEmpty()) {
            return Result.failure("Milestone is not found");
        }
        if (!isManager(userId, milestone.get().projectId())) {
            return Result.failure("Cannot manage milestones");
        }
        return action.apply(milestone.get());
    }
}
