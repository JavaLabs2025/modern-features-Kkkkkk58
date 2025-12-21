package org.lab.service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.lab.domain.common.Result;
import org.lab.domain.project.Project;
import org.lab.domain.user.Developer;
import org.lab.domain.user.Manager;
import org.lab.domain.user.ProjectMembership;
import org.lab.domain.user.TeamLead;
import org.lab.domain.user.Tester;
import org.lab.repository.MembershipRepository;
import org.lab.repository.ProjectRepository;
import org.lab.repository.UserRepository;

public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, 
                          MembershipRepository membershipRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    public Result<Project> createProject(String name, String description, UUID creatorId) {
        if (userRepository.findById(creatorId).isEmpty()) {
            return Result.failure("User is not found");
        }
        try {
            var project = Project.create(name, description, creatorId);
            projectRepository.save(project);
            membershipRepository.save(ProjectMembership.of(creatorId, project.id(), new Manager()));
            return Result.success(project);
        } catch (IllegalArgumentException e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Project> assignTeamLead(UUID projectId, UUID teamLeadId, UUID currentUserId) {
        return withManagerAccess(projectId, currentUserId, project -> {
            if (userRepository.findById(teamLeadId).isEmpty()) {
                return Result.failure("User is not found");
            }
            if (project.managerId().equals(teamLeadId)) {
                return Result.failure("Manager cannot be a team lead");
            }
            var updated = project.withTeamLead(teamLeadId);
            projectRepository.save(updated);
            membershipRepository.save(ProjectMembership.of(teamLeadId, projectId, new TeamLead()));
            return Result.success(updated);
        });
    }

    public Result<Project> addDeveloper(UUID projectId, UUID developerId, UUID currentUserId) {
        return withManagerAccess(projectId, currentUserId, project -> {
            if (userRepository.findById(developerId).isEmpty()) {
                return Result.failure("User is not found");
            }
            if (project.managerId().equals(developerId)) {
                return Result.failure("Manager cannot be a developer");
            }
            if (project.developerIds().contains(developerId)) {
                return Result.failure("User is already a developer");
            }
            var updated = project.withDeveloper(developerId);
            projectRepository.save(updated);
            membershipRepository.save(ProjectMembership.of(developerId, projectId, new Developer()));
            return Result.success(updated);
        });
    }

    public Result<Project> addTester(UUID projectId, UUID testerId, UUID currentUserId) {
        return withManagerAccess(projectId, currentUserId, project -> {
            if (userRepository.findById(testerId).isEmpty()) {
                return Result.failure("User is not found");
            }
            if (project.managerId().equals(testerId)) {
                return Result.failure("Manager cannot be a tester");
            }
            var updated = project.withTester(testerId);
            projectRepository.save(updated);
            membershipRepository.save(ProjectMembership.of(testerId, projectId, new Tester()));
            return Result.success(updated);
        });
    }

    public List<ProjectMembership> getProjectMembers(UUID projectId) {
        return membershipRepository.findByProjectId(projectId);
    }

    private Result<Project> withManagerAccess(
            UUID projectId,
            UUID currentUserId,
            Function<Project, Result<Project>> action
    ) {
        var project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            return Result.failure("Project is not found");
        }
        var role = membershipRepository.getRole(currentUserId, projectId);
        boolean isManager = role.map(r -> r instanceof Manager).orElse(false);

        if (!isManager) {
            return Result.failure("User is not permitted to access the operation");
        }
        return action.apply(project.get());
    }
}
