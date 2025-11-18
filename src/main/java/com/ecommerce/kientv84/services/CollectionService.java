package com.ecommerce.kientv84.services;


import com.ecommerce.kientv84.dtos.request.CollectionRequest;
import com.ecommerce.kientv84.dtos.request.CollectionUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchRequest;
import com.ecommerce.kientv84.dtos.response.CategoryResponse;
import com.ecommerce.kientv84.dtos.response.CollectionResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CollectionService {
    PagedResponse<CollectionResponse> getAllCollection(CollectionSearchRequest req);
    List<CollectionResponse> searchCollectionSuggestion(String q, int limit);
    CollectionResponse createCollection(CollectionRequest request);
    CollectionResponse getCollectionById(UUID uuid);
    CollectionResponse updateCollection(UUID uuid, CollectionUpdateRequest updateData);
    String deleteCollection(List<UUID> uuids);
}
