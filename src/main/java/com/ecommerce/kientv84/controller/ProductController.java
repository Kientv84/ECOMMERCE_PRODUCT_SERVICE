package com.ecommerce.kientv84.controller;

import com.ecommerce.kientv84.dtos.request.ProductRequest;
import com.ecommerce.kientv84.dtos.request.ProductUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.product.ProductSearchRequest;
import com.ecommerce.kientv84.dtos.response.CollectionResponse;
import com.ecommerce.kientv84.dtos.response.MaterialResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.dtos.response.ProductResponse;
import com.ecommerce.kientv84.services.ProductService;
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
public class ProductController {
    private final ProductService productService;

    @PostMapping("/items/filter")
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProduct(ProductSearchRequest req) {
        return ResponseEntity.ok(productService.getAllProduct(req));
    }

    @GetMapping("/items/suggestion")
    public ResponseEntity<List<ProductResponse>> getProductSuggestions(@RequestParam String q,
                                                                       @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(productService.searchProductSuggestion(q, limit));
    }

    @PostMapping("/item")
    public ResponseEntity<ProductResponse> createProduct(@Valid  @RequestBody ProductRequest productRequest) {
        return ResponseEntity.ok(productService.createProduct(productRequest));
    }

    @GetMapping("/item/{uuid}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(productService.getProductById(uuid));
    }

    @PostMapping("/item/{uuid}")
    public ResponseEntity<ProductResponse> updateProductById(@PathVariable UUID uuid, @Valid @RequestBody ProductUpdateRequest updateData) {
        return ResponseEntity.ok(productService.updateProductById(uuid, updateData));
    }

    @PostMapping("/items")
    public ResponseEntity<String> updateProductById( @RequestBody List<UUID> uuids) {
        return ResponseEntity.ok(productService.deleteProduct(uuids));
    }

    @PostMapping("/item/thumbnail_url/{id}")
    public ResponseEntity<ProductResponse> uploadThumbnail(
            @PathVariable UUID id,
            @RequestParam("thumbnail_url") MultipartFile thumbnailUrl
    ) {
        return ResponseEntity.ok(productService.uploadThumbnail(id, thumbnailUrl));
    }

    @DeleteMapping("/item/thumbnail_url/{id}")
    public ResponseEntity<ProductResponse> deleteThumbnailUrl(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.deleteThumbnailUrl(id));
    }
}
