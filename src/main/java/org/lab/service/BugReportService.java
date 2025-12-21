package org.lab.service;

import java.util.Map;
import java.util.UUID;

import org.lab.domain.common.Result;
import org.lab.domain.project.BugReport;
import org.lab.domain.project.BugStatus;
import org.lab.domain.user.Role;
import org.lab.repository.BugReportRepository;
import org.lab.repository.MembershipRepository;
import org.lab.repository.ProjectRepository;

public class BugReportService {

    private final BugReportRepository bugReportRepository;
    private final ProjectRepository projectRepository;
    private final MembershipRepository membershipRepository;

    public BugReportService(BugReportRepository bugReportRepository, ProjectRepository projectRepository,
                            MembershipRepository membershipRepository) {
        this.bugReportRepository = bugReportRepository;
        this.projectRepository = projectRepository;
        this.membershipRepository = membershipRepository;
    }

    public Result<BugReport> createBugReport(String title, String description, UUID projectId, UUID userId) {
        if (projectRepository.findById(projectId).isEmpty()) {
            return Result.failure("Project is not found");
        }

        var role = membershipRepository.getRole(userId, projectId);
        if (!role.map(Role::canCreateBugReports).orElse(false)) {
            return Result.failure("Cannot create bug reports");
        }

        try {
            var bug = BugReport.create(title, description, projectId, userId);
            bugReportRepository.save(bug);
            return Result.success(bug);
        } catch (IllegalArgumentException e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<BugReport> assignBug(UUID bugId, UUID assigneeId, UUID userId) {
        var bug = bugReportRepository.findById(bugId);
        if (bug.isEmpty()) {
            return Result.failure("Bug is not found");
        }

        var role = membershipRepository.getRole(userId, bug.get().projectId());
        if (!role.map(Role::canManageTickets).orElse(false)) {
            return Result.failure("Cannot assign bugs");
        }
        var assigneeRole = membershipRepository.getRole(assigneeId, bug.get().projectId());
        if (!assigneeRole.map(Role::canFixBugs).orElse(false)) {
            return Result.failure("Cannot fix bugs");
        }
        var updated = bug.get().withAssignee(assigneeId);
        bugReportRepository.save(updated);
        return Result.success(updated);
    }

    public Result<BugReport> markFixed(UUID bugId, UUID userId) {
        var bug = bugReportRepository.findById(bugId);
        if (bug.isEmpty()) {
            return Result.failure("Bug is not found");
        }

        var role = membershipRepository.getRole(userId, bug.get().projectId());
        if (!role.map(Role::canFixBugs).orElse(false)) {
            return Result.failure("Cannot fix bugs");
        }
        if (bug.get().status() != BugStatus.NEW) {
            return Result.failure("Incorrect bug status");
        }
        var updated = bug.get().markFixed();
        bugReportRepository.save(updated);
        return Result.success(updated);
    }

    public Result<BugReport> markTested(UUID bugId, UUID userId) {
        var bug = bugReportRepository.findById(bugId);
        if (bug.isEmpty()) return Result.failure("Bug is not found");

        var role = membershipRepository.getRole(userId, bug.get().projectId());
        if (!role.map(Role::canTestBugs).orElse(false)) {
            return Result.failure("Cannot test bugs");
        }
        if (bug.get().status() != BugStatus.FIXED) {
            return Result.failure("Incorrect bug status");
        }
        var updated = bug.get().markTested();
        bugReportRepository.save(updated);
        return Result.success(updated);
    }

    public Result<BugReport> closeBug(UUID bugId, UUID userId) {
        var bug = bugReportRepository.findById(bugId);
        if (bug.isEmpty()) {
            return Result.failure("Bug is not found");
        }

        var role = membershipRepository.getRole(userId, bug.get().projectId());
        if (!role.map(Role::canTestBugs).orElse(false)) {
            return Result.failure("Cannot close bugs");
        }
        if (bug.get().status() != BugStatus.TESTED) {
            return Result.failure("Incorrect bug status");
        }
        var updated = bug.get().close();
        bugReportRepository.save(updated);
        return Result.success(updated);
    }

    public Map<BugStatus, Long> getBugStats(UUID projectId) {
        return bugReportRepository.getStatusStats(projectId);
    }
}
