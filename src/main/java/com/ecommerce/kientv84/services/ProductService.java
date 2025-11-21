package com.ecommerce.kientv84.services;

import com.ecommerce.kientv84.dtos.request.DeleteImagesRequest;
import com.ecommerce.kientv84.dtos.request.ProductRequest;
import com.ecommerce.kientv84.dtos.request.ProductUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.product.ProductSearchRequest;
import com.ecommerce.kientv84.dtos.response.CategoryResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.dtos.response.ProductResponse;
import com.ecommerce.kientv84.entites.ProductEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    PagedResponse<ProductResponse> getAllProduct(ProductSearchRequest req);

    List<ProductResponse> searchProductSuggestion(String q, int limit);

    ProductResponse createProduct(ProductRequest productRequest);

    ProductResponse getProductById(UUID uuid);

    ProductResponse updateProductById(UUID uuid, ProductUpdateRequest updateData);

    String deleteProduct(List<UUID> uuids);

    //Sub function

    String generateNameProduct(ProductEntity productEntity);

    ProductResponse uploadImages(UUID id,List<MultipartFile> file);

    ProductResponse deleteImages(UUID productId, List<Integer> sortOrders);
}
