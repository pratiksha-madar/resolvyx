package com.resolvyx.resolvyx.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CategoryRequest {
    @NotBlank
    private String name;
}