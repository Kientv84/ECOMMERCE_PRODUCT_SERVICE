package com.ecommerce.kientv84.controller;

import com.ecommerce.kientv84.dtos.request.DeleteImagesRequest;
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
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProduct(@RequestBody ProductSearchRequest req) {
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
    public ResponseEntity<String> deleteProduct( @RequestBody List<UUID> uuids) {
        return ResponseEntity.ok(productService.deleteProduct(uuids));
    }

    @PostMapping("/item/images/{id}")
    public ResponseEntity<?> uploadImages(
            @PathVariable UUID id,
            @RequestParam("images") List<MultipartFile> files,
            @RequestParam(required = false) List<Integer> positions // optional: vị trí muốn ghi đè
    ) {
        return ResponseEntity.ok(productService.uploadImages(id, files, positions));
    }

    @DeleteMapping("/item/images/{id}")
    public ResponseEntity<ProductResponse> deleteImages(
            @PathVariable UUID id,
            @RequestBody DeleteImagesRequest request
    ) {
        return ResponseEntity.ok(productService.deleteImages(id, request.getSortOrders()));
    }
}
