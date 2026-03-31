package com.mentalhealthforum.forumservice.infrastructure.web;

import com.mentalhealthforum.forumservice.application.dto.CategoryDto;
import com.mentalhealthforum.forumservice.application.dto.CreateCategoryRequest;
import com.mentalhealthforum.forumservice.application.service.ForumApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final ForumApplicationService forumService;

    public CategoryController(ForumApplicationService forumService) {
        this.forumService = forumService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(forumService.getAllCategories());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryDto category = forumService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(forumService.getCategory(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        forumService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
