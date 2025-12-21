package org.lab.repository;

import java.util.Map;
import java.util.UUID;

import org.lab.domain.project.BugReport;
import org.lab.domain.project.BugStatus;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class BugReportRepository extends InMemoryRepository<BugReport> {

    public BugReportRepository() {
        super(BugReport::id);
    }

    public Map<BugStatus, Long> getStatusStats(UUID projectId) {
        return findAll(b -> b.projectId().equals(projectId)).stream()
                .collect(groupingBy(BugReport::status, counting()));
    }
}
