package com.resolvyx.resolvyx.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
public class FeedbackRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;
}