package com.resolvyx.resolvyx.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import com.resolvyx.resolvyx.entity.TicketStatus;

@Data
public class StatusUpdateRequest {
    @NotNull
    private TicketStatus status;
}