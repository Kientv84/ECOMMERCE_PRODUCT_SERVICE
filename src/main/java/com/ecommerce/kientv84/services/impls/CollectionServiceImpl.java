package com.ecommerce.kientv84.services.impls;

import com.ecommerce.kientv84.commons.Constant;
import com.ecommerce.kientv84.commons.EnumError;
import com.ecommerce.kientv84.commons.StatusEnum;
import com.ecommerce.kientv84.dtos.request.CollectionRequest;
import com.ecommerce.kientv84.dtos.request.CollectionUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.category.CategorySearchModel;
import com.ecommerce.kientv84.dtos.request.search.category.CategorySearchOption;
import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchModel;
import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchOption;
import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchRequest;
import com.ecommerce.kientv84.dtos.response.*;
import com.ecommerce.kientv84.dtos.response.CollectionResponse;
import com.ecommerce.kientv84.dtos.response.CollectionResponse;
import com.ecommerce.kientv84.entites.CategoryEntity;
import com.ecommerce.kientv84.entites.CollectionEntity;
import com.ecommerce.kientv84.exceptions.ServiceException;
import com.ecommerce.kientv84.mappers.CollectionMapper;
import com.ecommerce.kientv84.respositories.CollectionRepository;
import com.ecommerce.kientv84.services.CollectionService;
import com.ecommerce.kientv84.services.RedisService;
import com.ecommerce.kientv84.services.UploadFileProvider;
import com.ecommerce.kientv84.utils.PageableUtils;
import com.ecommerce.kientv84.utils.SpecificationBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CollectionServiceImpl implements CollectionService {
    private final CollectionMapper collectionMapper;
    private final CollectionRepository collectionRepository;
    private final RedisService redisService;
    private final UploadFileProvider uploadFileProvider;
    
    @Override
    public PagedResponse<CollectionResponse> getAllCollection(CollectionSearchRequest req) {
        log.info("Calling get all collection api ...");
        String key = "collections:list:"+ req.hashKey();
        try {
            // 1. check cache
            PagedResponse<CollectionResponse> cached = redisService.getValue(key, new TypeReference<PagedResponse<CollectionResponse>>() {
            });

            if (cached != null) {
                log.info("Redis read for key {}", key);
                return cached;
            }

            CollectionSearchModel model = req.getSearchModel();
            CollectionSearchOption option = req.getSearchOption();

            List<String> allowedFields = List.of("collectionName", "collectionCode" ,"createdDate");

            PageRequest pageRequest = PageableUtils.buildPageRequest(
                    option.getPage(),
                    option.getSize(),
                    option.getSort(),
                    allowedFields,
                    "createdDate",
                    Sort.Direction.DESC
            );

            Specification<CollectionEntity> spec = new SpecificationBuilder<CollectionEntity>()
                    .equal("status", model.getStatus())
                    .likeAnyFieldIgnoreCase(model.getQ(), "collectionCode")
                    .build();


            Page<CollectionResponse> result = collectionRepository.findAll(spec, pageRequest)
                    .map(collectionMapper::mapToCollectionResponse);


            PagedResponse<CollectionResponse> response = new PagedResponse<>(
                    result.getNumber(),
                    result.getSize(),
                    result.getTotalElements(),
                    result.getTotalPages(),
                    result.getContent()
            );

            redisService.setValue(key, response, Constant.SEARCH_CACHE_TTL);

            log.info("Redis MISS, caching search result for key {}", key);

            return response;

        } catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "category.get.error");
        }
    }

    @Override
    public List<CollectionResponse> searchCollectionSuggestion(String q, int limit) {
        List<CollectionEntity> collections = collectionRepository.searchCollectionSuggestion(q, limit);
        return collections.stream().map(col -> collectionMapper.mapToCollectionResponse(col)).toList();
    }


    @Override
    public CollectionResponse createCollection(CollectionRequest request) {
        try {
            CollectionEntity collectionEntity = collectionRepository.findCollectionByCollectionCode(request.getCollectionCode());

            if(collectionEntity != null) {
                throw new ServiceException(EnumError.COLLECTION_DATA_EXISTED, "collection.exit");
            }

            CollectionEntity newCollection = CollectionEntity.builder()
                    .collectionCode(request.getCollectionCode())
                    .collectionName(request.getCollectionName())
                    .status(request.getStatus())
                    .description(request.getDescription())
                    .build();

            collectionRepository.save(newCollection);

            redisService.deleteByKey("collections:list:*");

            return collectionMapper.mapToCollectionResponse(newCollection);

        } catch (ServiceException e) {
            throw e;
        } catch ( Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public CollectionResponse getCollectionById(UUID uuid) {
        log.info("Calling get by id api with collection {}", uuid);

        String key = "collection:"+uuid;

        try {
            // get from cache
            CollectionResponse cached = redisService.getValue(key, CollectionResponse.class);

            if (cached != null) {
                log.info("Redis get for key: {}", key);
                return cached;
            }

            CollectionEntity collectionEntity = collectionRepository.findById(uuid).orElseThrow(() -> new ServiceException(EnumError.COLLECTION_ERR_GET, "collection.get.error"));

            CollectionResponse response = collectionMapper.mapToCollectionResponse(collectionEntity);

            // storge redis
            redisService.setValue(key, response, Constant.CACHE_TTL);

            return response;
        } catch (ServiceException e) {
            throw e;
        } catch ( Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }

    }

    @Override
    public CollectionResponse updateCollection(UUID uuid, CollectionUpdateRequest updateData) {
        try {
            CollectionEntity collectionEntity = collectionRepository.findById(uuid).orElseThrow(() -> new ServiceException(EnumError.COLLECTION_ERR_GET, "collection.get.error"));

            if ( updateData.getCollectionCode() != null) {
                collectionEntity.setCollectionCode(updateData.getCollectionCode());
            }
            if ( updateData.getCollectionName() != null) {
                collectionEntity.setCollectionName(updateData.getCollectionName());
            }
            if ( updateData.getStatus() != null) {
                collectionEntity.setStatus(updateData.getStatus());
            }
            if ( updateData.getDescription() != null) {
                collectionEntity.setDescription(updateData.getDescription());
            }

            // Invalidate cache

            redisService.deleteByKeys("collection:" + uuid, "collections:list:*");

            log.info("Cache invalidated for key collection {}", uuid);

            return collectionMapper.mapToCollectionResponse(collectionEntity);

        } catch (ServiceException e) {
            throw e;
        } catch ( Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public String deleteCollection(List<UUID> uuids) {
        try {
            if ( uuids == null || uuids.isEmpty()) {
                throw new ServiceException(EnumError.COLLECTION_ERR_DEL_EM, "collection.delete.empty");
            }

            List<CollectionEntity> foundIds = collectionRepository.findAllById(uuids);

            if ( foundIds.isEmpty()) {
                throw new ServiceException(EnumError.COLLECTION_ERR_NOT_FOUND, "collection.delete.notfound");
            }


            // Soft delete:  update status
            foundIds.forEach(col -> col.setStatus(StatusEnum.DELETED.getStatus()));
            collectionRepository.saveAll(foundIds);

            //dete cache
            uuids.forEach(id -> redisService.deleteByKey("collection:"+id));
            redisService.deleteByKeys("collections:list:*");

            log.info("Deleted collections successfully and cache invalidated: {}", uuids);
            return "Deleted collections successfully: " + uuids;
        } catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }
}
