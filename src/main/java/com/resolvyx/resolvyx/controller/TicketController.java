package com.resolvyx.resolvyx.controller;

import com.resolvyx.resolvyx.dto.StatusUpdateRequest;
import com.resolvyx.resolvyx.dto.TicketRequest;
import com.resolvyx.resolvyx.dto.TicketResponse;
import com.resolvyx.resolvyx.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketRequest request, Authentication authentication) {
        return ResponseEntity.ok(ticketService.createTicket(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(ticketService.getTicketsForCurrentUser(authentication.getName()));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<TicketResponse> autoAssign(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.autoAssignTicket(id, authentication.getName()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request, Authentication authentication) {
        return ResponseEntity.ok(ticketService.updateStatus(id, request.getStatus(), authentication.getName()));
    }
}