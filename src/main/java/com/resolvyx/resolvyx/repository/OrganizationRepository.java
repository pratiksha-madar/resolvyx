package com.resolvyx.resolvyx.repository;

import com.resolvyx.resolvyx.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByOrgCode(String orgCode);
    boolean existsByOrgCode(String orgCode);
}