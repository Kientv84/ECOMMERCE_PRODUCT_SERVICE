package com.ecommerce.kientv84.services.impls;

import com.cloudinary.utils.ObjectUtils;
import com.ecommerce.kientv84.commons.Constant;
import com.ecommerce.kientv84.commons.EnumError;
import com.ecommerce.kientv84.commons.StatusEnum;
import com.ecommerce.kientv84.dtos.request.BrandRequest;
import com.ecommerce.kientv84.dtos.request.BrandUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.brand.BrandSearchModel;
import com.ecommerce.kientv84.dtos.request.search.brand.BrandSearchOption;
import com.ecommerce.kientv84.dtos.request.search.brand.BrandSearchRequest;
import com.ecommerce.kientv84.dtos.request.search.user.UserSearchModel;
import com.ecommerce.kientv84.dtos.request.search.user.UserSearchOption;
import com.ecommerce.kientv84.dtos.request.search.user.UserSearchRequest;
import com.ecommerce.kientv84.dtos.response.BrandResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.dtos.response.UserResponse;
import com.ecommerce.kientv84.entites.BrandEntity;
import com.ecommerce.kientv84.entites.UserEntity;
import com.ecommerce.kientv84.exceptions.ServiceException;
import com.ecommerce.kientv84.mappers.BrandMapper;
import com.ecommerce.kientv84.respositories.BrandRepository;
import com.ecommerce.kientv84.services.BrandService;
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

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BrandServiceImpl implements BrandService {
    private final BrandMapper brandMapper;
    private final BrandRepository brandRepository;
    private final RedisService redisService;
    private final UploadFileProvider uploadFileProvider;

    @Override
    public PagedResponse<BrandResponse> searchUsers(BrandSearchRequest req) {
        log.info("Get all brand api calling...");
        String key = "brands:list:" + req.hashKey();
        try {
            // 1. check brand
            PagedResponse<BrandResponse> cached =
                    redisService.getValue(key, new TypeReference<PagedResponse<BrandResponse>>() {});

            if (cached != null) {
                log.info("Redis read for key {}", key);
                return cached;
            }

            BrandSearchOption option = req.getSearchOption() ;
            BrandSearchModel model = req.getSearchModel();

            List<String> allowedFields = List.of("brandName", "createdDate");

            PageRequest pageRequest = PageableUtils.buildPageRequest(
                    option.getPage(),
                    option.getSize(),
                    option.getSort(),
                    allowedFields,
                    "createdDate",
                    Sort.Direction.DESC
            );

            Specification<BrandEntity> spec = new SpecificationBuilder<BrandEntity>()
                    .equal("status", model.getStatus())
                    .likeAnyFieldIgnoreCase(model.getQ(), "brandCode")
                    .build();

            Page<BrandResponse> result = brandRepository.findAll(spec, pageRequest)
                    .map(brandMapper::mapToBrandResponse);

            PagedResponse<BrandResponse> response = new PagedResponse<>(
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
            throw new ServiceException(EnumError.BRAND_ERR_GET, "brand.get.error");
        }
    }

    @Override
    public List<BrandResponse> searchUserSuggestion(String q, int limit) {
        List<BrandEntity> brands = brandRepository.searchBrandSuggestion(q, limit);
        return brands.stream().map(br -> brandMapper.mapToBrandResponse(br)).toList();
    }

    @Override
    public BrandResponse createBrand(BrandRequest brandRequest) {
        try {
            BrandEntity brand = brandRepository.findBrandByBrandCode(brandRequest.getBrandCode());

            if ( brand != null ) {
                throw new ServiceException(EnumError.BRAND_DATA_EXISTED, "brand.exit");
            }

            BrandEntity brandEntity = BrandEntity.builder()
                    .brandName(brandRequest.getBrandName())
                    .brandCode(brandRequest.getBrandCode())
                    .status(brandRequest.getStatus())
                    .description(brandRequest.getDescription())
                    .build();

            brandRepository.save(brandEntity);

            redisService.deleteByKey("brand:list:*");

            return brandMapper.mapToBrandResponse(brandEntity);

        } catch (ServiceException e) {
            throw e;
        } catch ( Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public BrandResponse getBrandById(UUID id) {
        log.info("Calling get by id api with brand {}", id);

        String key = "brand:"+id;

        try {
            // get from cache
            BrandResponse cached = redisService.getValue(key, BrandResponse.class);

            if (cached != null) {
                log.info("Redis get for key: {}", key);
                return cached;
            }

            BrandEntity brand = brandRepository.findById(id).orElseThrow(() -> new ServiceException(EnumError.BRAND_ERR_GET, "brand.get.error"));

            BrandResponse response =   brandMapper.mapToBrandResponse(brand);

            // storge redis
            redisService.setValue(key, response, Constant.CACHE_TTL);

            return  response;

        } catch (ServiceException e) {
            throw e ;
        } catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public BrandResponse updateBrandById(UUID id, BrandUpdateRequest updateData) {
        try {
            BrandEntity brand = brandRepository.findById(id).orElseThrow(() -> new ServiceException(EnumError.BRAND_ERR_GET, "brand.get.error"));

            if (updateData.getBrandName() != null) {
                brand.setBrandName(updateData.getBrandName());
            }
            if ( updateData.getBrandCode() != null) {
                brand.setBrandCode(updateData.getBrandCode());
            }
            if(updateData.getStatus() != null) {
                brand.setStatus(updateData.getStatus());
            }
            if(updateData.getDescription() != null) {
                brand.setDescription(updateData.getDescription());
            }

            BrandEntity saved = brandRepository.save(brand);

            // Invalidate cache
            String key = "brand:" + id;
            redisService.deleteByKey(key);

            redisService.deleteByKeys("brand:" + id, "brands:list:*");

            log.info("Cache invalidated for key {}", key);

            return  brandMapper.mapToBrandResponse(saved);

        } catch (ServiceException e) {
            throw e ;
        } catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public String deleteBrands(List<UUID> uuids) {
        try {
            if ( uuids == null || uuids.isEmpty()) {
                throw new ServiceException(EnumError.BRAND_ERR_DEL_EM, "brand.delete.empty");
            }

            // Lấy tất cả brand tồn tại
            List<BrandEntity> brands = brandRepository.findAllById(uuids);

            if (brands.isEmpty()) {
                throw new ServiceException(EnumError.BRAND_ERR_NOT_FOUND, "brand.delete.notfound");
            }

            // Soft delete:  update status
            brands.forEach(br -> br.setStatus(StatusEnum.DELETED.getStatus()));
            brandRepository.saveAll(brands);

            //dete cache
            uuids.forEach(uuid -> redisService.deleteByKey("brand:"+uuid));
            redisService.deleteByKeys("brand:list:*");

            log.info("Deleted brands successfully and cache invalidated: {}", uuids);
            return "Deleted brands successfully: " + uuids;

        } catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    // ===================== UPLOAD =====================


    @Override
    public BrandResponse uploadThumbnail(UUID id, MultipartFile thumbnailUrl) {
        BrandEntity brand = brandRepository.findById(id).orElseThrow(() -> new ServiceException(EnumError.BRAND_ERR_GET, "brand.get.error"));

        try {
            // Upload file lưu vào folder
            String folder = "brands/"+id;
            String thumbnail = uploadFileProvider.upload(thumbnailUrl, folder);

            // 2. Xóa avatar cũ nếu có
            if (brand.getThumbnailUrl() != null) {
                uploadFileProvider.deleteFileFromCloudinary(brand.getThumbnailUrl());
            }

            // 3. Lưu avatar mới
            brand.setThumbnailUrl(thumbnail);
            brandRepository.save(brand);

            return brandMapper.mapToBrandResponse(brand);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("have error to upload", e);
           throw new ServiceException(EnumError.UPLOAD_FAILED, "file.upload.fail");
        }
    }

    @Override
    public BrandResponse deleteThumnailUrl(UUID uuid) {
        BrandEntity brand = brandRepository.findById(uuid)
                .orElseThrow(() -> new ServiceException(EnumError.BRAND_ERR_NOT_FOUND, "brand.delete.notfound"));

        if (brand.getThumbnailUrl() != null) {
            uploadFileProvider.deleteFileFromCloudinary(brand.getThumbnailUrl());
            brand.setThumbnailUrl(null);
            brandRepository.save(brand);
        }

        return brandMapper.mapToBrandResponse(brand);
    }
}
