package com.resolvyx.resolvyx.service;

import com.resolvyx.resolvyx.dto.*;
import com.resolvyx.resolvyx.entity.*;
import com.resolvyx.resolvyx.repository.*;
import com.resolvyx.resolvyx.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Organization org = new Organization();
        org.setName(request.getOrganizationName());
        org.setOrgCode(generateOrgCode());
        org.setCreatedAt(LocalDateTime.now());
        organizationRepository.save(org);

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ORG_ADMIN);
        user.setOrganization(org);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), org.getId(), user.getRole().name());

        return new AuthResponse(token, user.getName(), user.getRole().name(), org.getName(), user.getId(), org.getOrgCode());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getOrganization().getId(), user.getRole().name());

        return new AuthResponse(token, user.getName(), user.getRole().name(), user.getOrganization().getName(), user.getId(), user.getOrganization().getOrgCode());
    }

    public AuthResponse join(JoinRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Organization org = organizationRepository.findByOrgCode(request.getOrgCode())
                .orElseThrow(() -> new RuntimeException("Invalid organization code"));

        Role role = request.getRole() != null ? request.getRole() : Role.MEMBER;
        if (role == Role.ORG_ADMIN) {
            throw new RuntimeException("Cannot join as ORG_ADMIN");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setOrganization(org);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), org.getId(), user.getRole().name());

        return new AuthResponse(token, user.getName(), user.getRole().name(), org.getName(), user.getId(), org.getOrgCode());
    }

    private String generateOrgCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (organizationRepository.existsByOrgCode(code));
        return code;
    }
}