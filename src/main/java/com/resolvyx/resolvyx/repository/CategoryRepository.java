package com.resolvyx.resolvyx.repository;

import com.resolvyx.resolvyx.entity.Category;
import com.resolvyx.resolvyx.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByOrganization(Organization organization);
}