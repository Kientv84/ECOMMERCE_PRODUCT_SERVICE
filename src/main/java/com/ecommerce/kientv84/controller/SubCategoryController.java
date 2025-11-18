package com.ecommerce.kientv84.controller;

import com.ecommerce.kientv84.dtos.request.SubCategoryRequest;
import com.ecommerce.kientv84.dtos.request.SubCategoryUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.subCategory.SubCategorySearchRequest;
import com.ecommerce.kientv84.dtos.response.CollectionResponse;
import com.ecommerce.kientv84.dtos.response.MaterialResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.dtos.response.SubCategoryResponse;
import com.ecommerce.kientv84.services.SubCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api")
@RequiredArgsConstructor
public class SubCategoryController {
    private final SubCategoryService subCategoryService;

    @PostMapping("/sub_categories/filter")
    public ResponseEntity<PagedResponse<SubCategoryResponse>> getAllSubCategory(SubCategorySearchRequest request) {
        return ResponseEntity.ok(subCategoryService.getAllSubCategory(request));
    }

    @GetMapping("/sub_categories/suggestion")
    public ResponseEntity<List<SubCategoryResponse>> getSubCategorySuggestions(@RequestParam String q,
                                                                       @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(subCategoryService.searchSubCategorySuggestion(q, limit));
    }


    @PostMapping("/sub_category")
    public ResponseEntity<SubCategoryResponse> createSubCategory(@Valid @RequestBody SubCategoryRequest request) {
        return ResponseEntity.ok(subCategoryService.createSubCategory(request));
    }

    @GetMapping("/sub_category/{id}")
    public ResponseEntity<SubCategoryResponse> getSubCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(subCategoryService.getSubCategoryById(id));
    }

    @PostMapping("/sub_category/{id}")
    public ResponseEntity<SubCategoryResponse> updateSubCategoryById(@PathVariable UUID id, @Valid @RequestBody SubCategoryUpdateRequest updateData) {
        return ResponseEntity.ok(subCategoryService.updateSubCategoryById(id, updateData));
    }

    @PostMapping("/sub_categories")
    public String deleteSubCategory(@RequestBody List<UUID> uuids) {
        return subCategoryService.deleteSubCategories(uuids);
    }


    @PostMapping("/material/thumbnail_url/{id}")
    public ResponseEntity<SubCategoryResponse> uploadThumbnail(
            @PathVariable UUID id,
            @RequestParam("thumbnail_url") MultipartFile thumbnailUrl
    ) {
        return ResponseEntity.ok(subCategoryService.uploadThumbnail(id, thumbnailUrl));
    }

    @DeleteMapping("/material/thumbnail_url/{id}")
    public ResponseEntity<SubCategoryResponse> deleteThumbnailUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(subCategoryService.deleteThumbnailUrl(id));
    }
}
