package com.resolvyx.resolvyx.controller;

import com.resolvyx.resolvyx.dto.TeamMemberResponse;
import com.resolvyx.resolvyx.entity.Organization;
import com.resolvyx.resolvyx.entity.TicketStatus;
import com.resolvyx.resolvyx.entity.User;
import com.resolvyx.resolvyx.repository.TicketRepository;
import com.resolvyx.resolvyx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @GetMapping
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<List<TeamMemberResponse>> listTeam(Authentication authentication) {
        User admin = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization org = admin.getOrganization();
        List<TicketStatus> activeStatuses = List.of(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS);

        List<TeamMemberResponse> team = userRepository.findByOrganization(org).stream()
                .map(u -> new TeamMemberResponse(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        u.getRole().name(),
                        ticketRepository.countByAssignedToAndStatusIn(u, activeStatuses)
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(team);
    }
}