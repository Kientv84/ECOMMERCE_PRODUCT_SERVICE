package com.ecommerce.kientv84.services;

import com.ecommerce.kientv84.dtos.request.SubCategoryRequest;
import com.ecommerce.kientv84.dtos.request.SubCategoryUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.subCategory.SubCategorySearchRequest;
import com.ecommerce.kientv84.dtos.response.CategoryResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.dtos.response.SubCategoryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface SubCategoryService {
    PagedResponse<SubCategoryResponse> getAllSubCategory(SubCategorySearchRequest req);

    List<SubCategoryResponse> searchSubCategorySuggestion(String q, int limit);

    SubCategoryResponse createSubCategory(SubCategoryRequest subSubCategoryRequest);

    SubCategoryResponse getSubCategoryById(UUID id);

    SubCategoryResponse updateSubCategoryById(UUID id, SubCategoryUpdateRequest updateData);

    String deleteSubCategories(List<UUID> ids);

    SubCategoryResponse uploadThumbnail(UUID id, MultipartFile thumbnailUrl);

    SubCategoryResponse deleteThumbnailUrl(UUID uuid);
}
