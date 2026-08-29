package com.resolvyx.resolvyx.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import com.resolvyx.resolvyx.entity.TicketStatus;
import com.resolvyx.resolvyx.entity.Urgency;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private Urgency urgency;
    private Integer priorityScore;
    private String categoryName;
    private String raisedByName;
    private String assignedToName;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Long raisedByUserId;
    private Integer feedbackRating;
    private String feedbackComment;
}