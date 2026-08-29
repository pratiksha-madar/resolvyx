package com.resolvyx.resolvyx.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String name;
    private String role;
    private String organizationName;
    private Long userId;
    private String orgCode;
}