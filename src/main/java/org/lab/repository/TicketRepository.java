package org.lab.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.lab.domain.project.Ticket;
import org.lab.domain.project.TicketStatus;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class TicketRepository extends InMemoryRepository<Ticket> {

    public TicketRepository() {
        super(Ticket::id);
    }

    public List<Ticket> findByMilestoneId(UUID milestoneId) {
        return findAll(t -> t.milestoneId().equals(milestoneId));
    }

    public long countIncomplete(UUID milestoneId) {
        return findByMilestoneId(milestoneId).stream().filter(t -> !t.isCompleted()).count();
    }

    public Map<TicketStatus, Long> getStatusStats(UUID milestoneId) {
        return findByMilestoneId(milestoneId).stream()
                .collect(groupingBy(Ticket::status, counting()));
    }
}
