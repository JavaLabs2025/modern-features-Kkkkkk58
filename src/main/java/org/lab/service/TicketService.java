package org.lab.service;

import java.util.Map;
import java.util.UUID;

import org.lab.domain.common.Result;
import org.lab.domain.project.Ticket;
import org.lab.domain.project.TicketStatus;
import org.lab.domain.user.Role;
import org.lab.repository.MembershipRepository;
import org.lab.repository.MilestoneRepository;
import org.lab.repository.ProjectRepository;
import org.lab.repository.TicketRepository;

public class TicketService {

    private final TicketRepository ticketRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final MembershipRepository membershipRepository;

    public TicketService(TicketRepository ticketRepository, MilestoneRepository milestoneRepository,
                         ProjectRepository projectRepository, MembershipRepository membershipRepository) {
        this.ticketRepository = ticketRepository;
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
        this.membershipRepository = membershipRepository;
    }

    public Result<Ticket> createTicket(String title, String description, UUID projectId, UUID milestoneId, UUID userId) {
        var role = membershipRepository.getRole(userId, projectId);
        if (!role.map(Role::canManageTickets).orElse(false)) {
            return Result.failure("Cannot create tickets");
        }
        var milestone = milestoneRepository.findById(milestoneId);
        if (milestone.isEmpty() || !milestone.get().projectId().equals(projectId)) {
            return Result.failure("Milestone is not found");
        }
        if (!milestone.get().canAddTickets()) {
            return Result.failure("Cannot add tickets to closed milestone");
        }
        try {
            var ticket = Ticket.create(title, description, projectId, milestoneId);
            ticketRepository.save(ticket);
            return Result.success(ticket);
        } catch (IllegalArgumentException e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Ticket> assignTicket(UUID ticketId, UUID assigneeId, UUID userId) {
        var ticket = ticketRepository.findById(ticketId);
        if (ticket.isEmpty()) return Result.failure("Ticket is not found");

        var role = membershipRepository.getRole(userId, ticket.get().projectId());
        if (!role.map(Role::canManageTickets).orElse(false)) {
            return Result.failure("Cannot assign tickets");
        }
        var project = projectRepository.findById(ticket.get().projectId());
        if (project.isEmpty() || !project.get().isDeveloper(assigneeId)) {
            return Result.failure("User cannot be assigned to the ticket");
        }
        var updated = ticket.get().withAssignee(assigneeId);
        ticketRepository.save(updated);
        return Result.success(updated);
    }

    public Result<Ticket> acceptTicket(UUID ticketId, UUID userId) {
        return updateTicketStatus(ticketId, userId, TicketStatus.NEW, Ticket::accept);
    }

    public Result<Ticket> startWork(UUID ticketId, UUID userId) {
        return updateTicketStatus(ticketId, userId, TicketStatus.ACCEPTED, Ticket::startWork);
    }

    public Result<Ticket> completeTicket(UUID ticketId, UUID userId) {
        return updateTicketStatus(ticketId, userId, TicketStatus.IN_PROGRESS, Ticket::complete);
    }

    public Map<TicketStatus, Long> getTicketStats(UUID milestoneId) {
        return ticketRepository.getStatusStats(milestoneId);
    }

    private Result<Ticket> updateTicketStatus(UUID ticketId, UUID userId, TicketStatus expected,
                                               java.util.function.Function<Ticket, Ticket> action) {
        var ticket = ticketRepository.findById(ticketId);
        if (ticket.isEmpty()) return Result.failure("Ticket is not found");

        var role = membershipRepository.getRole(userId, ticket.get().projectId());
        if (!role.map(Role::canExecuteTickets).orElse(false)) {
            return Result.failure("Cannot update status of tickets");
        }
        if (!ticket.get().isAssignedTo(userId)) {
            return Result.failure("Ticket assigned to another user");
        }
        if (ticket.get().status() != expected) {
            return Result.failure("Incorrect ticket status");
        }
        var updated = action.apply(ticket.get());
        ticketRepository.save(updated);
        return Result.success(updated);
    }
}
