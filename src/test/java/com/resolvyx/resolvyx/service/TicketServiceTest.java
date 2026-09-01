package com.resolvyx.resolvyx.service;

import com.resolvyx.resolvyx.dto.TicketRequest;
import com.resolvyx.resolvyx.dto.TicketResponse;
import com.resolvyx.resolvyx.entity.*;
import com.resolvyx.resolvyx.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private TicketService ticketService;

    private Organization org;
    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        org = new Organization();
        org.setId(1L);
        org.setName("Test Org");
        org.setOrgCode("TEST1234");

        user = new User();
        user.setId(1L);
        user.setEmail("admin@test.com");
        user.setRole(Role.ORG_ADMIN);
        user.setOrganization(org);

        category = new Category();
        category.setId(1L);
        category.setName("Water Supply");
        category.setOrganization(org);
    }

    @Test
    void createTicket_setsCorrectPriorityScore_forHighUrgency() {
        TicketRequest request = new TicketRequest();
        request.setTitle("No water");
        request.setDescription("Since morning");
        request.setCategoryId(1L);
        request.setUrgency(Urgency.HIGH);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(feedbackRepository.findByTicket(any())).thenReturn(Optional.empty());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        TicketResponse response = ticketService.createTicket(request, "admin@test.com");

        assertEquals(50, response.getPriorityScore());
        assertEquals(TicketStatus.OPEN, response.getStatus());
        assertEquals("Water Supply", response.getCategoryName());
    }

    @Test
    void createTicket_throwsException_whenCategoryBelongsToDifferentOrg() {
        Organization otherOrg = new Organization();
        otherOrg.setId(2L);
        category.setOrganization(otherOrg);

        TicketRequest request = new TicketRequest();
        request.setTitle("Test");
        request.setCategoryId(1L);
        request.setUrgency(Urgency.LOW);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ticketService.createTicket(request, "admin@test.com"));

        assertEquals("Category does not belong to your organization", exception.getMessage());
    }

    @Test
    void autoAssignTicket_pickStaffWithFewestActiveTickets() {
        User busyStaff = new User();
        busyStaff.setId(2L);
        busyStaff.setRole(Role.STAFF);
        busyStaff.setOrganization(org);

        User freeStaff = new User();
        freeStaff.setId(3L);
        freeStaff.setRole(Role.STAFF);
        freeStaff.setOrganization(org);

        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setOrganization(org);
        ticket.setCategory(category);
        ticket.setRaisedBy(user);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriorityScore(50);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findByOrganizationAndRole(org, Role.STAFF))
                .thenReturn(List.of(busyStaff, freeStaff));
        when(ticketRepository.countByAssignedToAndStatusIn(eq(busyStaff), any())).thenReturn(3L);
        when(ticketRepository.countByAssignedToAndStatusIn(eq(freeStaff), any())).thenReturn(0L);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(feedbackRepository.findByTicket(any())).thenReturn(Optional.empty());

        TicketResponse response = ticketService.autoAssignTicket(10L, "admin@test.com");

        assertEquals(TicketStatus.ASSIGNED, response.getStatus());
        assertEquals(freeStaff.getId(), ticket.getAssignedTo().getId());
    }

    @Test
    void autoAssignTicket_throwsException_whenNoStaffAvailable() {
        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setOrganization(org);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findByOrganizationAndRole(org, Role.STAFF)).thenReturn(List.of());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ticketService.autoAssignTicket(10L, "admin@test.com"));

        assertEquals("No staff members available in this organization", exception.getMessage());
    }

    @Test
    void submitFeedback_throwsException_whenTicketNotResolved() {
        Ticket ticket = new Ticket();
        ticket.setId(5L);
        ticket.setOrganization(org);
        ticket.setRaisedBy(user);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ticketService.submitFeedback(5L, 5, "Great!", "admin@test.com"));

        assertEquals("Feedback can only be left on resolved tickets", exception.getMessage());
    }

    @Test
    void submitFeedback_throwsException_whenNotTheTicketRaiser() {
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setEmail("other@test.com");
        otherUser.setOrganization(org);

        Ticket ticket = new Ticket();
        ticket.setId(5L);
        ticket.setOrganization(org);
        ticket.setRaisedBy(user);
        ticket.setStatus(TicketStatus.RESOLVED);

        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ticketService.submitFeedback(5L, 5, "Great!", "other@test.com"));

        assertEquals("Only the person who raised this ticket can leave feedback", exception.getMessage());
    }
}