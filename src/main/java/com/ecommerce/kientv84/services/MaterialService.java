package com.ecommerce.kientv84.services;

import com.ecommerce.kientv84.dtos.request.MaterialRequest;
import com.ecommerce.kientv84.dtos.request.MaterialUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.material.MaterialSearchRequest;
import com.ecommerce.kientv84.dtos.response.CategoryResponse;
import com.ecommerce.kientv84.dtos.response.MaterialResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MaterialService {
    PagedResponse<MaterialResponse> getAllMaterial(MaterialSearchRequest req);
    List<MaterialResponse> searchMaterialSuggestion(String q, int limit);
    MaterialResponse createMaterial(MaterialRequest request);
    MaterialResponse getMaterialById(UUID uuid);
    MaterialResponse updateMaterial(UUID uuid, MaterialUpdateRequest updateData);
    String deleteMaterial(List<UUID> uuids);
    MaterialResponse uploadThumbnail(UUID id, MultipartFile thumbnailUrl);

    MaterialResponse deleteThumbnailUrl(UUID uuid);
}
