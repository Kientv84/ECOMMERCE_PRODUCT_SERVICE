package com.ecommerce.kientv84.services.impls;

import com.ecommerce.kientv84.commons.EnumError;
import com.ecommerce.kientv84.dtos.request.ProductRequest;
import com.ecommerce.kientv84.dtos.request.ProductUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.product.ProductSearchRequest;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.dtos.response.ProductResponse;
import com.ecommerce.kientv84.entites.*;
import com.ecommerce.kientv84.exceptions.ServiceException;
import com.ecommerce.kientv84.mappers.ProductMapper;
import com.ecommerce.kientv84.messaging.producers.ProductProducer;
import com.ecommerce.kientv84.respositories.*;
import com.ecommerce.kientv84.services.ProductService;
import com.ecommerce.kientv84.services.RedisService;
import com.ecommerce.kientv84.services.UploadFileProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductServiceImp implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final BrandRepository brandRepository;
    private final CollectionRepository collectionRepository;
    private final MaterialRepository materialRepository;
    private final ProductMapper productMapper;
    private final ProductProducer productProducer;
    private final RedisService redisService;
    private final UploadFileProvider uploadFileProvider;

    @Override
    public PagedResponse<ProductResponse> getAllProduct(ProductSearchRequest req) {
        return null;
    }

    @Override
    public List<ProductResponse> searchProductSuggestion(String q, int limit) {
        return List.of();
    }

    @Override
    public ProductResponse uploadThumbnail(UUID id, MultipartFile thumbnailUrl) {
        return null;
    }

    @Override
    public ProductResponse deleteThumbnailUrl(UUID uuid) {
        return null;
    }

    @Override
    public ProductResponse createProduct(ProductRequest productRequest) {
        try {

            CategoryEntity category = categoryRepository.findById(productRequest.getCategory())
                    .orElseThrow(() -> new ServiceException(EnumError.CATE_ERR_GET, "category.get.error"));

            SubCategoryEntity subCategory = subCategoryRepository.findById(productRequest.getSubCategory())
                    .orElseThrow(() -> new ServiceException(EnumError.SUB_CATE_ERR_GET, "sub.category.get.error"));

            BrandEntity brand = brandRepository.findById(productRequest.getBrand())
                    .orElseThrow(() -> new ServiceException(EnumError.BRAND_ERR_GET, "brand.get.error"));

            CollectionEntity collection = collectionRepository.findById(productRequest.getCollection())
                    .orElseThrow(() -> new ServiceException(EnumError.COLLECTION_ERR_GET, "collection.get.error"));

            MaterialEntity material = materialRepository.findById(productRequest.getMaterial())
                    .orElseThrow(() -> new ServiceException(EnumError.MATERIAL_ERR_GET, "material.get.error"));

            ProductEntity productEntity = ProductEntity.builder()
                    .brand(brand)
                    .category(category)
                    .productCode(productRequest.getProductCode())
                    .subCategory(subCategory)
                    .basePrice(productRequest.getBasePrice())
                    .status(productRequest.getStatus())
                    .careInstruction(productRequest.getCareInstruction())
                    .fitType(productRequest.getFitType())
                    .description(productRequest.getDescription())
                    .stock(productRequest.getStock())
                    .material(material)
                    .collection(collection)
                    .ratingCount(productRequest.getRatingCount())
                    .careInstruction(productRequest.getCareInstruction())
                    .createdBy("ADMIN")
                    .discountPercent(productRequest.getDiscountPercent())
                    .origin(productRequest.getOrigin())
                    .thumbnailUrl(productRequest.getThumbnailUrl())
                    .ratingAverage(productRequest.getRatingAverage())
                    .createdDate(new Date())
                    .build();

            String generatedName = generateNameProduct(productEntity);

            productEntity.setProductName(generatedName);

            productRepository.save(productEntity);

            // Product kafka tạo tồn kho
            productProducer.produceInventoryCreate(productMapper.mapToKafkaInventoryRequest(productEntity));

            return productMapper.mapToProductResponse(productEntity);
        } catch (ServiceException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "product.get.error" );
        }
    }

    @Override
    public ProductResponse getProductById(UUID uuid) {
        try {
            ProductEntity productEntity = productRepository.findById(uuid).orElseThrow(() -> new ServiceException(EnumError.PRO_ERR_GET, "product.get.error", new Object[]{}));

            return productMapper.mapToProductResponse(productEntity);

        } catch (ServiceException e) {
            //các lỗi business (do bạn chủ động ném ra)
            throw e;
        } catch (Exception e) {
            // Bọc lại các lỗi hệ thống khác
            throw new ServiceException(EnumError.PRO_ERR_GET, "product.get.error");
        }
    }

    @Override
    public ProductResponse updateProductById(UUID uuid, ProductUpdateRequest updateData) {

        try {

            ProductEntity productEntity = productRepository.findById(uuid).orElseThrow(() -> new ServiceException(EnumError.PRO_ERR_GET, "product.get.error"));


           if ( updateData.getBrand() != null) {
               productEntity.setBrand(productEntity.getBrand());
           }
            if ( updateData.getCategory() != null) {
                CategoryEntity category = categoryRepository.findById(updateData.getCategory())
                        .orElseThrow(() -> new ServiceException(EnumError.CATE_ERR_GET, "category.get.error"));
                productEntity.setCategory(category);
            }
            if ( updateData.getSubCategory() != null) {
                SubCategoryEntity subCategory = subCategoryRepository.findById(updateData.getSubCategory())
                        .orElseThrow(() -> new ServiceException(EnumError.SUB_CATE_ERR_GET, "sub.category.get.error"));
                productEntity.setSubCategory(subCategory);
            }
            if ( updateData.getBasePrice() != null) {
                BrandEntity brand = brandRepository.findById(updateData.getBrand())
                        .orElseThrow(() -> new ServiceException(EnumError.BRAND_ERR_GET, "brand.get.error"));
                productEntity.setBrand(brand);
            }
            if ( updateData.getStatus() != null) {
                productEntity.setStatus(productEntity.getStatus());
            }
            if ( updateData.getCollection() != null) {
                CollectionEntity collection = collectionRepository.findById(updateData.getCollection())
                        .orElseThrow(() -> new ServiceException(EnumError.COLLECTION_ERR_GET, "collection.get.error"));
            }
            if ( updateData.getMaterial() != null) {
                MaterialEntity material = materialRepository.findById(updateData.getMaterial())
                        .orElseThrow(() -> new ServiceException(EnumError.MATERIAL_ERR_GET, "material.get.error"));
            }
            if ( updateData.getDescription() != null) {
                productEntity.setDescription(updateData.getDescription());
            }
            if (updateData.getStatus() != null) {
                productEntity.setStatus(updateData.getStatus());
            }
            if (updateData.getBasePrice() != null) {
                productEntity.setBasePrice(updateData.getBasePrice());
            }
            if (updateData.getDescription() != null) {
                productEntity.setDescription(updateData.getDescription());
            }
            if (updateData.getCareInstruction() != null) {
                productEntity.setCareInstruction(updateData.getCareInstruction());
            }
            if (updateData.getProductCode() != null) {
                productEntity.setProductCode(updateData.getProductCode());
            }
            if (updateData.getFitType() != null) {
                productEntity.setFitType(updateData.getFitType());
            }
            if (updateData.getRatingAverage() != null) {
                productEntity.setRatingAverage(updateData.getRatingAverage());
            }
            if (updateData.getThumbnailUrl() != null) {
                productEntity.setThumbnailUrl(updateData.getThumbnailUrl());
            }

            String name = generateNameProduct(productEntity);

            productEntity.setProductName(name);

            productRepository.save(productEntity);

            return productMapper.mapToProductResponse(productEntity);


        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(EnumError.INTERNAL_ERROR, "sys.internal.error");
        }
    }

    @Override
    public String deleteProduct(List<UUID> uuids) {
        try {

            if ( uuids == null && uuids.isEmpty()) {
                throw new ServiceException(
                        EnumError.PRO_ERR_DEL_EM,
                       "product.delete.empty",
                        new Object[]{}
                );
            }

            List<ProductEntity> products = productRepository.findAllById(uuids);

            Set<UUID> productSet = products.stream().map(ProductEntity::getId).collect(Collectors.toSet());

            List<UUID> notFoundIds = uuids.stream()
                    .filter(id -> !productSet.contains(id))
                    .toList();

            if (!notFoundIds.isEmpty()) {
                throw new ServiceException(
                        EnumError.PRO_ERR_NOT_FOUND,
                        "product.delete.notfound" + notFoundIds,
                        new Object[]{notFoundIds.toString()}
                );
            }

            productRepository.deleteAllById(uuids);
            log.info("Deleted products successfully: {}", uuids);

            return "Deleted products successfully: {}" + uuids;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // SUB FUNCTION
    @Override
    public String generateNameProduct(ProductEntity productEntity) {
        return String.format("%s %s %s",
                productEntity.getBrand().getBrandName().trim(),
                productEntity.getCategory().getCategoryName().trim(),
                productEntity.getSubCategory().getSubCategoryCode().trim()
        ).trim();
    }

}
