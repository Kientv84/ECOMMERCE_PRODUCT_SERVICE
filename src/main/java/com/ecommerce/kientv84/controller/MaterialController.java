package com.ecommerce.kientv84.controller;

import com.ecommerce.kientv84.dtos.request.CollectionRequest;
import com.ecommerce.kientv84.dtos.request.CollectionUpdateRequest;
import com.ecommerce.kientv84.dtos.request.MaterialRequest;
import com.ecommerce.kientv84.dtos.request.MaterialUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.material.MaterialSearchRequest;
import com.ecommerce.kientv84.dtos.response.CategoryResponse;
import com.ecommerce.kientv84.dtos.response.CollectionResponse;
import com.ecommerce.kientv84.dtos.response.MaterialResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.services.MaterialService;
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
public class MaterialController {
    private final MaterialService materialService;

    @PostMapping("/materials/filter")
    public ResponseEntity<PagedResponse<MaterialResponse>> getAllMaterial(MaterialSearchRequest req) {
        return ResponseEntity.ok(materialService.getAllMaterial(req));
    }

    @GetMapping("/materials/suggestion")
    public ResponseEntity<List<MaterialResponse>> getMaterialSuggestions(@RequestParam String q,
                                                                       @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(materialService.searchMaterialSuggestion(q, limit));
    }

    @PostMapping("/material")
    public ResponseEntity<MaterialResponse> createMaterial(@Valid @RequestBody MaterialRequest request) {
        return ResponseEntity.ok(materialService.createMaterial(request));
    }

    @GetMapping("/material/{uuid}")
    public ResponseEntity<MaterialResponse> getMaterialById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(materialService.getMaterialById(uuid));
    }

    @PostMapping("/material/{uuid}")
    public ResponseEntity<MaterialResponse> updateMaterialById(@PathVariable UUID uuid, @RequestBody MaterialUpdateRequest updateData) {
        return ResponseEntity.ok(materialService.updateMaterial(uuid, updateData));
    }

    @PostMapping("/materials")
    public String deleteMaterial(@RequestBody List<UUID> uuids) {
        return materialService.deleteMaterial(uuids);
    }

    @PostMapping("/material/thumbnail_url/{id}")
    public ResponseEntity<MaterialResponse> uploadThumbnail(
            @PathVariable UUID id,
            @RequestParam("thumbnail_url") MultipartFile thumbnailUrl
    ) {
        return ResponseEntity.ok(materialService.uploadThumbnail(id, thumbnailUrl));
    }

    @DeleteMapping("/material/thumbnail_url/{id}")
    public ResponseEntity<MaterialResponse> deleteThumbnailUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(materialService.deleteThumbnailUrl(id));
    }
}
