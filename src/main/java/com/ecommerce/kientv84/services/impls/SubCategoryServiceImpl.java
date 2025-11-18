package com.ecommerce.kientv84.services.impls;

import com.ecommerce.kientv84.commons.EnumError;
import com.ecommerce.kientv84.dtos.request.SubCategoryRequest;
import com.ecommerce.kientv84.dtos.request.SubCategoryUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.subCategory.SubCategorySearchRequest;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.dtos.response.SubCategoryResponse;
import com.ecommerce.kientv84.entites.CategoryEntity;
import com.ecommerce.kientv84.entites.SubCategoryEntity;
import com.ecommerce.kientv84.exceptions.ServiceException;
import com.ecommerce.kientv84.mappers.SubCategoryMapper;
import com.ecommerce.kientv84.respositories.CategoryRepository;
import com.ecommerce.kientv84.respositories.SubCategoryRepository;
import com.ecommerce.kientv84.services.RedisService;
import com.ecommerce.kientv84.services.SubCategoryService;
import com.ecommerce.kientv84.services.UploadFileProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        return null;
    }

    @Override
    public List<SubCategoryResponse> searchSubCategorySuggestion(String q, int limit) {
        return List.of();
    }

    @Override
    public SubCategoryResponse uploadThumbnail(UUID id, MultipartFile thumbnailUrl) {
        return null;
    }

    @Override
    public SubCategoryResponse deleteThumbnailUrl(UUID uuid) {
        return null;
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

            return subCategoryMapper.mapToSubCategoryResponse(subCategory);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(EnumError.SUB_CATE_ERR_GET, "sub.category.get.error");
        }
    }

    @Override
    public SubCategoryResponse getSubCategoryById(UUID id) {

        try {
            SubCategoryEntity subCategory = subCategoryRepository.findById(id).orElseThrow(() -> new ServiceException(EnumError.SUB_CATE_ERR_GET, "sub.category.get.error"));

            return subCategoryMapper.mapToSubCategoryResponse(subCategory);

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

            subCategoryRepository.save(subCategory);

            return subCategoryMapper.mapToSubCategoryResponse(subCategory);

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

            Set<UUID> foundIds = idsFound.stream().map(SubCategoryEntity::getId).collect(Collectors.toSet());

            List<UUID> notFoundIds = ids.stream().filter(uuid -> !foundIds.contains(uuid)).toList();

            if ( !notFoundIds.isEmpty()) {
                throw new ServiceException(
                        EnumError.ACC_ERR_NOT_FOUND,
                        "sub.category.delete.notfound" + notFoundIds,
                        new Object[]{notFoundIds.toString()}
                );
            }

            subCategoryRepository.deleteAllById(ids);

            return  "Deleted sub categories successfully: {}" + ids;

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }
}
