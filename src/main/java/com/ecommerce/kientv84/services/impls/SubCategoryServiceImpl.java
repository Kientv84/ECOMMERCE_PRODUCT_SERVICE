package com.ecommerce.kientv84.services.impls;

import com.ecommerce.kientv84.commons.Constant;
import com.ecommerce.kientv84.commons.EnumError;
import com.ecommerce.kientv84.commons.StatusEnum;
import com.ecommerce.kientv84.dtos.request.SubCategoryRequest;
import com.ecommerce.kientv84.dtos.request.SubCategoryUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.subCategory.SubCategorySearchModel;
import com.ecommerce.kientv84.dtos.request.search.subCategory.SubCategorySearchOption;
import com.ecommerce.kientv84.dtos.request.search.subCategory.SubCategorySearchRequest;
import com.ecommerce.kientv84.dtos.response.BrandResponse;
import com.ecommerce.kientv84.dtos.response.SubCategoryResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.entites.BrandEntity;
import com.ecommerce.kientv84.entites.SubCategoryEntity;
import com.ecommerce.kientv84.entites.CategoryEntity;
import com.ecommerce.kientv84.entites.SubCategoryEntity;
import com.ecommerce.kientv84.exceptions.ServiceException;
import com.ecommerce.kientv84.mappers.SubCategoryMapper;
import com.ecommerce.kientv84.respositories.CategoryRepository;
import com.ecommerce.kientv84.respositories.SubCategoryRepository;
import com.ecommerce.kientv84.services.RedisService;
import com.ecommerce.kientv84.services.SubCategoryService;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubCategoryServiceImpl implements SubCategoryService {

    private final SubCategoryMapper subCategoryMapper;
    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final RedisService redisService;
    private final UploadFileProvider uploadFileProvider;

    @Override
    public PagedResponse<SubCategoryResponse> getAllSubCategory(SubCategorySearchRequest req) {
        log.info("Get all subCategory api calling...");
        String key = "subCategories:list:" + req.hashKey();
        try {
            // 1. check subCategory
            PagedResponse<SubCategoryResponse> cached =
                    redisService.getValue(key, new TypeReference<PagedResponse<SubCategoryResponse>>() {});

            if (cached != null) {
                log.info("Redis read for key {}", key);
                return cached;
            }

            SubCategorySearchOption option = req.getSearchOption() ;
            SubCategorySearchModel model = req.getSearchModel();

            List<String> allowedFields = List.of("subCategoryName", "createdDate");

            PageRequest pageRequest = PageableUtils.buildPageRequest(
                    option.getPage(),
                    option.getSize(),
                    option.getSort(),
                    allowedFields,
                    "createdDate",
                    Sort.Direction.DESC
            );

            Specification<SubCategoryEntity> spec = new SpecificationBuilder<SubCategoryEntity>()
                    .equal("status", model.getStatus())
                    .likeAnyFieldIgnoreCase(model.getQ(), "subCategoryCode")
                    .build();

            Page<SubCategoryResponse> result = subCategoryRepository.findAll(spec, pageRequest)
                    .map(subCategoryMapper::mapToSubCategoryResponse);

            PagedResponse<SubCategoryResponse> response = new PagedResponse<>(
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
            throw new ServiceException(EnumError.SUB_CATE_ERR_GET, "sub.category.get.error");
        }
    }

    @Override
    public List<SubCategoryResponse> searchSubCategorySuggestion(String q, int limit) {
        List<SubCategoryEntity> subCategoryEntities = subCategoryRepository.searchSubCategorySuggestion(q, limit);
        return subCategoryEntities.stream().map(sub -> subCategoryMapper.mapToSubCategoryResponse(sub)).toList();
    }

    @Override
    public SubCategoryResponse uploadThumbnail(UUID id, MultipartFile thumbnailUrl) {
        SubCategoryEntity subCategory = subCategoryRepository.findById(id).orElseThrow(() -> new ServiceException(EnumError.SUB_CATE_ERR_GET, "sub.category.get.error"));

        try {
            // Upload file lưu vào folder
            String folder = "sub_categories/"+id;
            String thumbnail = uploadFileProvider.upload(thumbnailUrl, folder);

            // 2. Xóa avatar cũ nếu có
            if (subCategory.getThumbnailUrl() != null) {
                uploadFileProvider.deleteFileFromCloudinary(subCategory.getThumbnailUrl());
            }

            // 3. Lưu avatar mới
            subCategory.setThumbnailUrl(thumbnail);
            subCategoryRepository.save(subCategory);

            return subCategoryMapper.mapToSubCategoryResponse(subCategory);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("have error to upload", e);
            throw new ServiceException(EnumError.UPLOAD_FAILED, "file.upload.fail");
        }
    }

    @Override
    public SubCategoryResponse deleteThumbnailUrl(UUID uuid) {
        SubCategoryEntity subCategory = subCategoryRepository.findById(uuid)
                .orElseThrow(() -> new ServiceException(EnumError.SUB_CATE_ERR_GET, "sub.category.get.error"));

        if (subCategory.getThumbnailUrl() != null) {
            uploadFileProvider.deleteFileFromCloudinary(subCategory.getThumbnailUrl());
            subCategory.setThumbnailUrl(null);
            subCategoryRepository.save(subCategory);
        }

        return subCategoryMapper.mapToSubCategoryResponse(subCategory);
    }

    @Override
    public SubCategoryResponse createSubCategory(SubCategoryRequest request) {
        try {
            SubCategoryEntity entity = subCategoryRepository.findSubCategoryBySubCategoryCode(request.subCategoryCode);
            if ( entity != null ) {
                throw new ServiceException(EnumError.SUB_CATE_DATA_EXISTED, "sub.category.exit");
            }

            CategoryEntity category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new ServiceException(EnumError.CATE_ERR_GET, "category.get.error"));

            SubCategoryEntity subCategory = SubCategoryEntity.builder()
                    .subCategoryName(request.getSubCategoryName())
                    .subCategoryCode(request.getSubCategoryCode())
                    .category(category)
                    .status(request.getStatus())
                    .build();

            subCategoryRepository.save(subCategory);

            SubCategoryResponse saved = subCategoryMapper.mapToSubCategoryResponse(subCategory);
            // delete key
            redisService.deleteByKey("subCategories:list:*");

            return saved;

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(EnumError.SUB_CATE_ERR_GET, "sub.category.get.error");
        }
    }

    @Override
    public SubCategoryResponse getSubCategoryById(UUID id) {
        log.info("Calling get by id api with sub category {}", id);

        String key = "subCategory:"+id;

        try {
            // get from cache
            SubCategoryResponse cached = redisService.getValue(key, SubCategoryResponse.class);

            if (cached != null) {
                log.info("Redis get for key: {}", key);
                return cached;
            }

            SubCategoryEntity subCategory = subCategoryRepository.findById(id).orElseThrow(() -> new ServiceException(EnumError.SUB_CATE_ERR_GET, "sub.category.get.error"));

            SubCategoryResponse response = subCategoryMapper.mapToSubCategoryResponse(subCategory);

            // storge redis
            redisService.setValue(key, response, Constant.CACHE_TTL);

            return  response;

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public SubCategoryResponse updateSubCategoryById(UUID id, SubCategoryUpdateRequest updateData) {
        try {
            SubCategoryEntity subCategory = subCategoryRepository.findById(id).orElseThrow(() -> new ServiceException(EnumError.SUB_CATE_ERR_GET, "sub.category.get.error"));

            if ( updateData.getSubCategoryName() != null ) {
                subCategory.setSubCategoryName(updateData.getSubCategoryName());
            }
            if ( updateData.getSubCategoryCode() != null) {
                subCategory.setSubCategoryCode(updateData.getSubCategoryCode());
            }
            if ( updateData.getStatus() != null ) {
                subCategory.setStatus(updateData.getStatus());
            }
            if ( updateData.getCategory() != null) {
                CategoryEntity category = categoryRepository.findById(updateData.getCategory())
                        .orElseThrow(() -> new ServiceException(EnumError.CATE_ERR_GET, "category.get.error"));
                subCategory.setCategory(category);
            }

            SubCategoryEntity saved =  subCategoryRepository.save(subCategory);

            // Invalidate cache

            redisService.deleteByKeys("subCategory:" + id, "subCategories:list:*");

            return  subCategoryMapper.mapToSubCategoryResponse(saved);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public String deleteSubCategories(List<UUID> ids) {

        try {
            if ( ids == null || ids.isEmpty()) {
                throw new ServiceException(EnumError.SUB_CATE_ERR_DEL_EM ,"sub.category.delete.empty");
            }

            List<SubCategoryEntity> idsFound = subCategoryRepository.findAllById(ids);

            if (idsFound.isEmpty()) {
                throw new ServiceException(EnumError.SUB_CATE_ERR_NOT_FOUND, "sub.category.delete.notfound");
            }

            // Soft delete:  update status
            idsFound.forEach(sub -> sub.setStatus(StatusEnum.DELETED.getStatus()));
            subCategoryRepository.saveAll(idsFound);

            //dete cache
            ids.forEach(uuid -> redisService.deleteByKey("subCategory:"+uuid));

            redisService.deleteByKeys("subCategories:list:*");

            log.info("Deleted subCategories successfully and cache invalidated: {}", ids);

            return "Deleted subCategories successfully: " + ids;

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }
}
