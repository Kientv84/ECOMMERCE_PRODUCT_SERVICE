package com.ecommerce.kientv84.services.impls;

import com.ecommerce.kientv84.commons.Constant;
import com.ecommerce.kientv84.commons.EnumError;
import com.ecommerce.kientv84.commons.StatusEnum;
import com.ecommerce.kientv84.dtos.request.MaterialRequest;
import com.ecommerce.kientv84.dtos.request.MaterialUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.material.MaterialSearchModel;
import com.ecommerce.kientv84.dtos.request.search.material.MaterialSearchOption;
import com.ecommerce.kientv84.dtos.request.search.material.MaterialSearchRequest;
import com.ecommerce.kientv84.dtos.response.BrandResponse;
import com.ecommerce.kientv84.dtos.response.MaterialResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.entites.BrandEntity;
import com.ecommerce.kientv84.entites.MaterialEntity;
import com.ecommerce.kientv84.exceptions.ServiceException;
import com.ecommerce.kientv84.mappers.MaterialMapper;
import com.ecommerce.kientv84.respositories.MaterialRepository;
import com.ecommerce.kientv84.services.MaterialService;
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
public class MaterialServiceImpl implements MaterialService {
    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;
    private final RedisService redisService;
    private final UploadFileProvider uploadFileProvider;

    @Override
    public PagedResponse<MaterialResponse> getAllMaterial(MaterialSearchRequest req) {
        log.info("Get all material api calling...");
        String key = "materials:list:" + req.hashKey();
        try {
            // 1. check material
            PagedResponse<MaterialResponse> cached =
                    redisService.getValue(key, new TypeReference<PagedResponse<MaterialResponse>>() {});

            if (cached != null) {
                log.info("Redis read for key {}", key);
                return cached;
            }

            MaterialSearchOption option = req.getSearchOption() ;
            MaterialSearchModel model = req.getSearchModel();

            List<String> allowedFields = List.of("materialName", "createdDate");

            PageRequest pageRequest = PageableUtils.buildPageRequest(
                    option.getPage(),
                    option.getSize(),
                    option.getSort(),
                    allowedFields,
                    "createdDate",
                    Sort.Direction.DESC
            );

            Specification<MaterialEntity> spec = new SpecificationBuilder<MaterialEntity>()
                    .equal("status", model.getStatus())
                    .likeAnyFieldIgnoreCase(model.getQ(), "materialCode")
                    .build();

            Page<MaterialResponse> result = materialRepository.findAll(spec, pageRequest)
                    .map(materialMapper::mapToMaterialResponse);

            PagedResponse<MaterialResponse> response = new PagedResponse<>(
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
            throw new ServiceException(EnumError.MATERIAL_ERR_GET, "material.get.error");
        }
    }

    @Override
    public List<MaterialResponse> searchMaterialSuggestion(String q, int limit) {
        List<MaterialEntity> brands = materialRepository.searchMaterialSuggestion(q, limit);
        return brands.stream().map(ma -> materialMapper.mapToMaterialResponse(ma)).toList();
    }

    @Override
    public MaterialResponse createMaterial(MaterialRequest request) {
        try {
            MaterialEntity material = materialRepository.findMaterialByMaterialCode(request.getMaterialCode());

            if(material != null) {
                throw new ServiceException(EnumError.MATERIAL_DATA_EXISTED, "material.exit");
            }

            MaterialEntity newMaterial = MaterialEntity.builder()
                    .materialCode(request.getMaterialCode())
                    .materialName(request.getMaterialName())
                    .status(request.getStatus())
                    .description(request.getDescription())
                    .build();

            materialRepository.save(newMaterial);

            redisService.deleteByKey("materials:list:*");

            return materialMapper.mapToMaterialResponse(newMaterial);


        } catch (ServiceException e) {
            throw e;
        } catch ( Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public MaterialResponse getMaterialById(UUID uuid) {

        log.info("Calling get by id api with material {}", uuid);

        String key = "material:"+uuid;

        try {
            // get from cache
            MaterialResponse cached = redisService.getValue(key, MaterialResponse.class);

            if (cached != null) {
                log.info("Redis get for key: {}", key);
                return cached;
            }

            MaterialEntity materialEntity = materialRepository.findById(uuid).orElseThrow(() -> new ServiceException(EnumError.MATERIAL_ERR_GET, "material.get.error"));

            MaterialResponse response =   materialMapper.mapToMaterialResponse(materialEntity);

            // storge redis
            redisService.setValue(key, response, Constant.CACHE_TTL);

            return  response;
        } catch (ServiceException e) {
            throw e;
        } catch ( Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public MaterialResponse updateMaterial(UUID uuid, MaterialUpdateRequest updateData) {
        try {
            MaterialEntity materialEntity = materialRepository.findById(uuid).orElseThrow(() -> new ServiceException(EnumError.MATERIAL_ERR_GET, "material.get.error"));

            if ( updateData.getMaterialCode() != null) {
                materialEntity.setMaterialCode(updateData.getMaterialCode());
            }
            if ( updateData.getMaterialName() != null) {
                materialEntity.setMaterialName(updateData.getMaterialName());
            }
            if ( updateData.getStatus() != null) {
                materialEntity.setStatus(updateData.getStatus());
            }
            if ( updateData.getDescription() != null) {
                materialEntity.setDescription(updateData.getDescription());
            }

//            materialRepository.save(materialEntity);
//
//            return materialMapper.mapToMaterialResponse(materialEntity);

            MaterialEntity saved = materialRepository.save(materialEntity);

            // Invalidate cache
            redisService.deleteByKeys("material:" + uuid, "material:list:*");

            return  materialMapper.mapToMaterialResponse(saved);

        } catch (ServiceException e) {
            throw e;
        } catch ( Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public String deleteMaterial(List<UUID> uuids) {
        try {
            if ( uuids == null || uuids.isEmpty()) {
                throw new ServiceException(EnumError.MATERIAL_ERR_DEL_EM, "material.delete.empty");
            }

            List<MaterialEntity> foundIds = materialRepository.findAllById(uuids);

            if ( foundIds.isEmpty()) {
                throw new ServiceException(EnumError.MATERIAL_ERR_NOT_FOUND, "material.delete.nottfound");
            }


            // Soft delete:  update status
            foundIds.forEach(ma -> ma.setStatus(StatusEnum.DELETED.getStatus()));
            materialRepository.saveAll(foundIds);

            //dete cache
            uuids.forEach(uuid -> redisService.deleteByKey("material:"+uuid));
            redisService.deleteByKeys("materials:list:*");

            log.info("Deleted materials successfully and cache invalidated: {}", uuids);
            return "Deleted materials successfully: " + uuids;

        } catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }
}
