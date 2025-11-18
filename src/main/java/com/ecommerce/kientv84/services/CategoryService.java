package com.ecommerce.kientv84.services;

import com.ecommerce.kientv84.dtos.request.CategoryRequest;
import com.ecommerce.kientv84.dtos.request.CategoryUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.brand.BrandSearchRequest;
import com.ecommerce.kientv84.dtos.request.search.category.CategorySearchRequest;
import com.ecommerce.kientv84.dtos.response.BrandResponse;
import com.ecommerce.kientv84.dtos.response.CategoryResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    PagedResponse<CategoryResponse> getAllCategory(CategorySearchRequest req);

    CategoryResponse crateCategory(CategoryRequest categoryRequest);

    List<CategoryResponse> searchUserSuggestion(String q, int limit);

    CategoryResponse getCategoryById(UUID id);

    CategoryResponse updateCategoryById(UUID id, CategoryUpdateRequest updateData);

    String deleteCategories(List<UUID> ids);

    CategoryResponse uploadThumbnail(UUID id, MultipartFile thumbnailUrl);

    CategoryResponse deleteThumbnailUrl(UUID uuid);
}
