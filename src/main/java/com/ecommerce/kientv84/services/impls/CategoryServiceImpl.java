package com.ecommerce.kientv84.services.impls;

import com.ecommerce.kientv84.commons.Constant;
import com.ecommerce.kientv84.commons.EnumError;
import com.ecommerce.kientv84.commons.StatusEnum;
import com.ecommerce.kientv84.dtos.request.CategoryRequest;
import com.ecommerce.kientv84.dtos.request.CategoryUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.category.CategorySearchModel;
import com.ecommerce.kientv84.dtos.request.search.category.CategorySearchOption;
import com.ecommerce.kientv84.dtos.request.search.category.CategorySearchRequest;
import com.ecommerce.kientv84.dtos.response.BrandResponse;
import com.ecommerce.kientv84.dtos.response.CategoryResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.entites.BrandEntity;
import com.ecommerce.kientv84.entites.CategoryEntity;
import com.ecommerce.kientv84.exceptions.ServiceException;
import com.ecommerce.kientv84.mappers.CategoryMapper;
import com.ecommerce.kientv84.respositories.CategoryRepository;
import com.ecommerce.kientv84.services.CategoryService;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final RedisService redisService;
    private final UploadFileProvider uploadFileProvider;


    @Override
    public PagedResponse<CategoryResponse> getAllCategory(CategorySearchRequest req) {
        log.info("Calling get all category api ...");
        String key = "categories:list:"+ req.hashKey();
        try {
            // 1. check cache
            PagedResponse<CategoryResponse> cached = redisService.getValue(key, new TypeReference<PagedResponse<CategoryResponse>>() {
           });

            if (cached != null) {
                log.info("Redis read for key {}", key);
                return cached;
            }

            CategorySearchModel model = req.getSearchModel();
            CategorySearchOption option = req.getSearchOption();

            List<String> allowedFields = List.of("categoryName", "categoryCode" ,"createdDate");

            PageRequest pageRequest = PageableUtils.buildPageRequest(
                    option.getPage(),
                    option.getSize(),
                    option.getSort(),
                    allowedFields,
                    "createdDate",
                    Sort.Direction.DESC
            );

            Specification<CategoryEntity> spec = new SpecificationBuilder<CategoryEntity>()
                    .equal("status", model.getStatus())
                    .likeAnyFieldIgnoreCase(model.getQ(), "categoryCode")
                    .build();


            Page<CategoryResponse> result = categoryRepository.findAll(spec, pageRequest)
                    .map(categoryMapper::mapToCategoryResponse);


            PagedResponse<CategoryResponse> response = new PagedResponse<>(
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
    public List<CategoryResponse> searchCategorySuggestion(String q, int limit) {
        List<CategoryEntity> categoryEntities = categoryRepository.searchCategorySuggestion(q, limit);
        return categoryEntities.stream().map(br -> categoryMapper.mapToCategoryResponse(br)).toList();
    }

    @Override
    public CategoryResponse crateCategory(CategoryRequest categoryRequest) {
        try {
            CategoryEntity findCategory = categoryRepository.findCategoryByCategoryCode(categoryRequest.getCategoryCode());

            if ( findCategory != null ) {
                throw new ServiceException(EnumError.CATE_DATA_EXISTED, "category.exit");
            }

            CategoryEntity newCategory = CategoryEntity.builder()
                    .categoryName(categoryRequest.getCategoryName())
                    .categoryCode(categoryRequest.getCategoryCode())
                    .status(categoryRequest.getStatus())
                    .description(categoryRequest.getDescription())
                    .build();

            categoryRepository.save(newCategory);

            redisService.deleteByKey("categories:list:*");

            return categoryMapper.mapToCategoryResponse(newCategory);

        } catch (ServiceException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR);
        }
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {
        log.info("Calling get by id api with category {}", id);

        String key = "category:"+id;

        try {
            // get from cache
            CategoryResponse cached = redisService.getValue(key, CategoryResponse.class);

            if (cached != null) {
                log.info("Redis get for key: {}", key);
                return cached;
            }

            CategoryEntity category = categoryRepository.findById(id).orElseThrow(() -> new ServiceException(EnumError.CATE_ERR_GET, "category.get.error"));

            CategoryResponse response =  categoryMapper.mapToCategoryResponse(category);

            // storge redis
            redisService.setValue(key, response, Constant.CACHE_TTL);

            return  response;
        } catch (ServiceException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public CategoryResponse updateCategoryById(UUID id, CategoryUpdateRequest updateData) {
        try {
            CategoryEntity category = categoryRepository.findById(id).orElseThrow(() -> new ServiceException(EnumError.CATE_ERR_GET, "category.get.error"));

           if (updateData.getCategoryName() != null) {
               category.setCategoryName(updateData.getCategoryName());
           }
           if ( updateData.getCategoryCode() != null) {
               category.setCategoryCode(updateData.getCategoryCode());
           }
           if(updateData.getStatus() != null) {
               category.setStatus(updateData.getStatus());
           }

            CategoryEntity saved = categoryRepository.save(category);

            // Invalidate cache
            String key = "category:" + id;
            redisService.deleteByKey(key);

            redisService.deleteByKeys("category:" + id, "categories:list:*");

            log.info("Cache invalidated for key {}", key);

            return  categoryMapper.mapToCategoryResponse(saved);
        } catch (ServiceException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public String deleteCategories(List<UUID> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                throw new ServiceException(EnumError.CATE_ERR_DEL_EM, "category.delete.empty");
            }

            List<CategoryEntity> categories = categoryRepository.findAllById(ids);

            if (categories.isEmpty()) {
                throw new ServiceException(EnumError.CATE_ERR_NOT_FOUND, "category.delete.notfound");
            }

            // Soft delete:  update status
            categories.forEach(cate -> cate.setStatus(StatusEnum.DELETED.getStatus()));
            categoryRepository.saveAll(categories);

            //dete cache
            ids.forEach(id -> redisService.deleteByKey("category:"+id));
            redisService.deleteByKeys("categories:list:*");

            log.info("Deleted categories successfully and cache invalidated: {}", ids);
            return "Deleted categories successfully: " + ids;

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public CategoryResponse uploadThumbnail(UUID id, MultipartFile thumbnailUrl) {
        CategoryEntity category = categoryRepository.findById(id).orElseThrow(() -> new ServiceException(EnumError.CATE_ERR_GET, "category.get.error"));

        try {
            // Upload file lưu vào folder
            String folder = "categories/"+id;
            String thumbnail = uploadFileProvider.upload(thumbnailUrl, folder);

            // 2. Xóa avatar cũ nếu có
            if (category.getThumbnailUrl() != null) {
                uploadFileProvider.deleteFileFromCloudinary(category.getThumbnailUrl());
            }

            // 3. Lưu avatar mới
            category.setThumbnailUrl(thumbnail);
            categoryRepository.save(category);

            return categoryMapper.mapToCategoryResponse(category);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("have error to upload", e);
            throw new ServiceException(EnumError.UPLOAD_FAILED, "file.upload.fail");
        }
    }

    @Override
    public CategoryResponse deleteThumbnailUrl(UUID uuid) {
        CategoryEntity category = categoryRepository.findById(uuid)
                .orElseThrow(() -> new ServiceException(EnumError.CATE_ERR_NOT_FOUND, "category.delete.notfound"));

        if (category.getThumbnailUrl() != null) {
            uploadFileProvider.deleteFileFromCloudinary(category.getThumbnailUrl());
            category.setThumbnailUrl(null);
            categoryRepository.save(category);
        }

        return categoryMapper.mapToCategoryResponse(category);
    }
}
