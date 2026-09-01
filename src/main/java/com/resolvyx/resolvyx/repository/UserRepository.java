package com.resolvyx.resolvyx.repository;

import com.resolvyx.resolvyx.entity.User;
import com.resolvyx.resolvyx.entity.Organization;
import com.resolvyx.resolvyx.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByOrganizationAndRole(Organization organization, Role role);
    List<User> findByOrganization(Organization organization);
}