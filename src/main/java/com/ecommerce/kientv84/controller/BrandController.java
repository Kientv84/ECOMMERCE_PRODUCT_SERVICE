package com.ecommerce.kientv84.controller;

import com.ecommerce.kientv84.dtos.request.BrandRequest;
import com.ecommerce.kientv84.dtos.request.BrandUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.brand.BrandSearchRequest;
import com.ecommerce.kientv84.dtos.request.search.user.UserSearchRequest;
import com.ecommerce.kientv84.dtos.response.BrandResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.dtos.response.UserResponse;
import com.ecommerce.kientv84.mappers.BrandMapper;
import com.ecommerce.kientv84.services.BrandService;
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
public class BrandController {
    private final BrandService brandService;

    @PostMapping("/brands/filter")
    public ResponseEntity<PagedResponse<BrandResponse>> searchBrands(@RequestBody BrandSearchRequest req) {
        return ResponseEntity.ok(brandService.searchBrands(req));
    }

    @GetMapping("/brands/suggestion")
    public ResponseEntity<List<BrandResponse>> getBrandSuggestions(@RequestParam String q,
                                                                 @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(brandService.searchBrandSuggestion(q, limit));
    }

    @PostMapping("/brand")
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest brandRequest) {
        return ResponseEntity.ok(brandService.createBrand(brandRequest));
    }

    @GetMapping("/brand/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable UUID id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @PostMapping("/brand/{id}")
    public ResponseEntity<BrandResponse> updateBrandById(@PathVariable UUID id, @RequestBody BrandUpdateRequest updateData) {
        return ResponseEntity.ok(brandService.updateBrandById(id, updateData));
    }

    @PostMapping("/brands")
    public String deleteBrand( @RequestBody List<UUID> uuids) {
        return brandService.deleteBrands(uuids);
    }

    @PostMapping("/brand/thumbnail_url/{id}")
    public ResponseEntity<BrandResponse> uploadAvatar(
            @PathVariable UUID id,
            @RequestParam("thumbnail_url") MultipartFile thumbnailUrl
    ) {
        return ResponseEntity.ok(brandService.uploadThumbnail(id, thumbnailUrl));
    }

    @DeleteMapping("/brand/thumbnail_url/{id}")
    public ResponseEntity<BrandResponse> deleteAvatar(@PathVariable UUID id) {
        return ResponseEntity.ok(brandService.deleteThumnailUrl(id));
    }
}
