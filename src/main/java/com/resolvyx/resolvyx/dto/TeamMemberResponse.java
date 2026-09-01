package com.resolvyx.resolvyx.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class TeamMemberResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private long activeTicketCount;
}