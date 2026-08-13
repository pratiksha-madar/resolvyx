package com.resolvyx.resolvyx.controller;

import com.resolvyx.resolvyx.dto.CategoryRequest;
import com.resolvyx.resolvyx.entity.Category;
import com.resolvyx.resolvyx.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest request, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(categoryService.createCategory(request, email));
    }

    @GetMapping
    public ResponseEntity<List<Category>> list(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(categoryService.getCategoriesForCurrentUser(email));
    }
}