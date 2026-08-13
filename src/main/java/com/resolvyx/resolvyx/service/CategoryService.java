package com.resolvyx.resolvyx.service;

import com.resolvyx.resolvyx.dto.CategoryRequest;
import com.resolvyx.resolvyx.entity.Category;
import com.resolvyx.resolvyx.entity.Organization;
import com.resolvyx.resolvyx.entity.User;
import com.resolvyx.resolvyx.repository.CategoryRepository;
import com.resolvyx.resolvyx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    public Category createCategory(CategoryRequest request, String email) {
        Organization org = getOrganizationOfUser(email);

        Category category = new Category();
        category.setName(request.getName());
        category.setOrganization(org);

        return categoryRepository.save(category);
    }

    public List<Category> getCategoriesForCurrentUser(String email) {
        Organization org = getOrganizationOfUser(email);
        return categoryRepository.findByOrganization(org);
    }

    private Organization getOrganizationOfUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getOrganization();
    }
}