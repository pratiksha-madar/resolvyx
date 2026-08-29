package com.resolvyx.resolvyx.service;

import com.resolvyx.resolvyx.dto.AnalyticsResponse;
import com.resolvyx.resolvyx.dto.TicketRequest;
import com.resolvyx.resolvyx.dto.TicketResponse;
import com.resolvyx.resolvyx.entity.*;
import com.resolvyx.resolvyx.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    public TicketResponse createTicket(TicketRequest request, String email) {
        User raisedBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Organization org = raisedBy.getOrganization();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getOrganization().getId().equals(org.getId())) {
            throw new RuntimeException("Category does not belong to your organization");
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setUrgency(request.getUrgency());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriorityScore(calculatePriorityScore(request.getUrgency()));
        ticket.setSlaDeadline(calculateSlaDeadline(request.getUrgency()));
        ticket.setOrganization(org);
        ticket.setCategory(category);
        ticket.setRaisedBy(raisedBy);
        ticket.setCreatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    public List<TicketResponse> getTicketsForCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ticketRepository.findByOrganization(user.getOrganization())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TicketResponse autoAssignTicket(Long ticketId, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!ticket.getOrganization().getId().equals(admin.getOrganization().getId())) {
            throw new RuntimeException("Ticket does not belong to your organization");
        }

        List<User> staffMembers = userRepository.findByOrganizationAndRole(admin.getOrganization(), Role.STAFF);
        if (staffMembers.isEmpty()) {
            throw new RuntimeException("No staff members available in this organization");
        }

        List<TicketStatus> activeStatuses = List.of(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS);

        User leastBusyStaff = staffMembers.stream()
                .min(Comparator.comparingLong(staff -> ticketRepository.countByAssignedToAndStatusIn(staff, activeStatuses)))
                .orElseThrow(() -> new RuntimeException("No staff available"));

        ticket.setAssignedTo(leastBusyStaff);
        ticket.setStatus(TicketStatus.ASSIGNED);

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    public TicketResponse updateStatus(Long ticketId, TicketStatus newStatus, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!ticket.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new RuntimeException("Ticket does not belong to your organization");
        }

        boolean isAssignedStaff = ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ORG_ADMIN;
        if (!isAssignedStaff && !isAdmin) {
            throw new RuntimeException("Only the assigned staff or an admin can update this ticket");
        }

        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    public TicketResponse submitFeedback(Long ticketId, Integer rating, String comment, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!ticket.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new RuntimeException("Ticket does not belong to your organization");
        }

        if (!ticket.getRaisedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Only the person who raised this ticket can leave feedback");
        }

        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new RuntimeException("Feedback can only be left on resolved tickets");
        }

        if (feedbackRepository.existsByTicket(ticket)) {
            throw new RuntimeException("Feedback has already been submitted for this ticket");
        }

        Feedback feedback = new Feedback();
        feedback.setTicket(ticket);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setCreatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);

        return toResponse(ticket);
    }

    public AnalyticsResponse getAnalytics(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Ticket> tickets = ticketRepository.findByOrganization(user.getOrganization());

        long total = tickets.size();
        long open = tickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
        long inProgress = tickets.stream().filter(t -> t.getStatus() == TicketStatus.ASSIGNED || t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long resolved = tickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count();
        long escalated = tickets.stream().filter(Ticket::isEscalated).count();

        Double avgResolutionHours = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.RESOLVED && t.getResolvedAt() != null)
                .mapToLong(t -> ChronoUnit.HOURS.between(t.getCreatedAt(), t.getResolvedAt()))
                .average()
                .stream().boxed().findFirst().orElse(null);

        List<Feedback> allFeedback = tickets.stream()
                .map(feedbackRepository::findByTicket)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();

        Double avgRating = allFeedback.isEmpty() ? null :
                allFeedback.stream().mapToInt(Feedback::getRating).average().orElse(0);

        Map<String, Long> byCategory = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().getName(), LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> byStaff = tickets.stream()
                .filter(t -> t.getAssignedTo() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignedTo().getName(), LinkedHashMap::new, Collectors.counting()));

        return new AnalyticsResponse(total, open, inProgress, resolved, escalated, avgResolutionHours, avgRating, byCategory, byStaff);
    }

    @Scheduled(fixedRate = 60000)
    public void checkSlaBreaches() {
        List<Ticket> allOpenTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() != TicketStatus.RESOLVED && !t.isEscalated())
                .toList();

        for (Ticket ticket : allOpenTickets) {
            if (ticket.getSlaDeadline() != null && LocalDateTime.now().isAfter(ticket.getSlaDeadline())) {
                ticket.setEscalated(true);
                ticket.setPriorityScore(ticket.getPriorityScore() + 50);
                ticketRepository.save(ticket);
                System.out.println("SLA BREACHED — escalated ticket #" + ticket.getId() + ": " + ticket.getTitle());
            }
        }
    }

    private int calculatePriorityScore(Urgency urgency) {
        return switch (urgency) {
            case LOW -> 10;
            case MEDIUM -> 25;
            case HIGH -> 50;
            case CRITICAL -> 100;
        };
    }

    private LocalDateTime calculateSlaDeadline(Urgency urgency) {
        LocalDateTime now = LocalDateTime.now();
        return switch (urgency) {
            case CRITICAL -> now.plusHours(6);
            case HIGH -> now.plusHours(24);
            case MEDIUM -> now.plusHours(48);
            case LOW -> now.plusHours(72);
        };
    }

    private TicketResponse toResponse(Ticket t) {
        Feedback feedback = feedbackRepository.findByTicket(t).orElse(null);
        return new TicketResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getUrgency(),
                t.getPriorityScore(),
                t.getCategory().getName(),
                t.getRaisedBy().getName(),
                t.getAssignedTo() != null ? t.getAssignedTo().getName() : null,
                t.getCreatedAt(),
                t.getResolvedAt(),
                t.getRaisedBy().getId(),
                feedback != null ? feedback.getRating() : null,
                feedback != null ? feedback.getComment() : null
        );
    }
}