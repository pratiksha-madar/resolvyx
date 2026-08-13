package com.resolvyx.resolvyx.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.resolvyx.resolvyx.entity.Urgency;

@Data
public class TicketRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Long categoryId;

    @NotNull
    private Urgency urgency;
}