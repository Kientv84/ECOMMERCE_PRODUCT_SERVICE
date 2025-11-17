package com.ecommerce.kientv84.services;

import com.ecommerce.kientv84.dtos.request.BrandRequest;
import com.ecommerce.kientv84.dtos.request.BrandUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.brand.BrandSearchRequest;
import com.ecommerce.kientv84.dtos.request.search.user.UserSearchRequest;
import com.ecommerce.kientv84.dtos.response.BrandResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BrandService {
    PagedResponse<BrandResponse> searchUsers(BrandSearchRequest req);

    List<BrandResponse> searchUserSuggestion(String q, int limit);

    BrandResponse createBrand(BrandRequest brandRequest);

    BrandResponse getBrandById(UUID id);

    BrandResponse updateBrandById(UUID id, BrandUpdateRequest updateData);

    String deleteBrands(List<UUID> uuids);

    BrandResponse uploadThumbnail(UUID id, MultipartFile thumbnailUrl);

    BrandResponse deleteThumnailUrl(UUID uuid);
}
