package com.resolvyx.resolvyx.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import com.resolvyx.resolvyx.entity.Role;

@Data
public class JoinRequest {
    @NotBlank
    private String orgCode;

    @NotBlank
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;

    private Role role; // STAFF or MEMBER — defaults to MEMBER if not sent
}