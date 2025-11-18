package com.ecommerce.kientv84.controller;

import com.ecommerce.kientv84.dtos.request.CollectionRequest;
import com.ecommerce.kientv84.dtos.request.CollectionUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchRequest;
import com.ecommerce.kientv84.dtos.response.BrandResponse;
import com.ecommerce.kientv84.dtos.response.CollectionResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.services.CollectionService;
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
public class CollectionController {
    private final CollectionService collectionService;

    @PostMapping("/collections/filter")
    public ResponseEntity<PagedResponse<CollectionResponse>> getAllCollection(CollectionSearchRequest req) {
        return ResponseEntity.ok(collectionService.getAllCollection(req));
    }

    @GetMapping("/collections/suggestion")
    public ResponseEntity<List<CollectionResponse>> getCollectionSuggestions(@RequestParam String q,
                                                                  @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(collectionService.searchCollectionSuggestion(q, limit));
    }

    @PostMapping("/collection")
    public ResponseEntity<CollectionResponse> createCollection(@Valid @RequestBody CollectionRequest request) {
        return ResponseEntity.ok(collectionService.createCollection(request));
    }

    @GetMapping("/collection/{uuid}")
    public ResponseEntity<CollectionResponse> getCollectionById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(collectionService.getCollectionById(uuid));
    }

    @PostMapping("/collection/{uuid}")
    public ResponseEntity<CollectionResponse> updateCollectionById(@PathVariable UUID uuid, @RequestBody CollectionUpdateRequest updateData) {
        return ResponseEntity.ok(collectionService.updateCollection(uuid, updateData));
    }

    @PostMapping("/collections")
    public String deleteCollection(@RequestBody List<UUID> uuids) {
        return collectionService.deleteCollection(uuids);
    }

}
