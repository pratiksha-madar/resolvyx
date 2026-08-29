package com.resolvyx.resolvyx.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AnalyticsResponse {
    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long escalatedTickets;
    private Double averageResolutionHours;
    private Double averageRating;
    private Map<String, Long> ticketsByCategory;
    private Map<String, Long> ticketsByStaff;
}