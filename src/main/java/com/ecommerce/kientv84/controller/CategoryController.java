package com.ecommerce.kientv84.controller;

import com.ecommerce.kientv84.dtos.request.CategoryRequest;
import com.ecommerce.kientv84.dtos.request.CategoryUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.category.CategorySearchRequest;
import com.ecommerce.kientv84.dtos.response.BrandResponse;
import com.ecommerce.kientv84.dtos.response.CategoryResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/categories/filter")
    public ResponseEntity<PagedResponse<CategoryResponse>> getAllCategory(CategorySearchRequest req) {
        return ResponseEntity.ok(categoryService.getAllCategory(req));
    }

    @GetMapping("/categories/suggestion")
    public ResponseEntity<List<CategoryResponse>> getUserSuggestions(@RequestParam String q,
                                                                  @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(categoryService.searchCategorySuggestion(q, limit));
    }

    @PostMapping("/category")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        return ResponseEntity.ok(categoryService.crateCategory(categoryRequest));
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping("/category/{id}")
    public ResponseEntity<CategoryResponse> updateCategoryById(@PathVariable UUID id, @RequestBody CategoryUpdateRequest updateData) {
        return ResponseEntity.ok(categoryService.updateCategoryById(id, updateData));
    }

    @PostMapping("/categories")
    public String updateCategoryById(@RequestBody List<UUID> ids) {
        return categoryService.deleteCategories(ids);
    }

    @PostMapping("/category/thumbnail_url/{id}")
    public ResponseEntity<CategoryResponse> uploadThumbnail(
            @PathVariable UUID id,
            @RequestParam("thumbnail_url") MultipartFile thumbnailUrl
    ) {
        return ResponseEntity.ok(categoryService.uploadThumbnail(id, thumbnailUrl));
    }

    @DeleteMapping("/category/thumbnail_url/{id}")
    public ResponseEntity<CategoryResponse> deleteThumbnailUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.deleteThumbnailUrl(id));
    }
}
