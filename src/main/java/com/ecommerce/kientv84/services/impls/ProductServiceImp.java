package com.ecommerce.kientv84.services.impls;

import com.ecommerce.kientv84.commons.Constant;
import com.ecommerce.kientv84.commons.EnumError;
import com.ecommerce.kientv84.commons.StatusEnum;
import com.ecommerce.kientv84.dtos.request.DeleteImagesRequest;
import com.ecommerce.kientv84.dtos.request.ProductRequest;
import com.ecommerce.kientv84.dtos.request.ProductUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.brand.BrandSearchModel;
import com.ecommerce.kientv84.dtos.request.search.brand.BrandSearchOption;
import com.ecommerce.kientv84.dtos.request.search.product.ProductSearchModel;
import com.ecommerce.kientv84.dtos.request.search.product.ProductSearchOption;
import com.ecommerce.kientv84.dtos.request.search.product.ProductSearchRequest;
import com.ecommerce.kientv84.dtos.response.BrandResponse;
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
import com.ecommerce.kientv84.utils.PageableUtils;
import com.ecommerce.kientv84.utils.SpecificationBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
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
    private final ProductImageRepository productImageRepository;

    @Override
    public PagedResponse<ProductResponse> getAllProduct(ProductSearchRequest req) {
        log.info("Get all product api calling...");
        String key = "products:list:" + req.hashKey();
        try {
            // 1. check product
            PagedResponse<ProductResponse> cached =
                    redisService.getValue(key, new TypeReference<PagedResponse<ProductResponse>>() {});

            if (cached != null) {
                log.info("Redis read for key {}", key);
                return cached;
            }

            ProductSearchOption option = req.getSearchOption() ;
            ProductSearchModel model = req.getSearchModel();

            List<String> allowedFields = List.of("productName", "createdDate");

            PageRequest pageRequest = PageableUtils.buildPageRequest(
                    option.getPage(),
                    option.getSize(),
                    option.getSort(),
                    allowedFields,
                    "createdDate",
                    Sort.Direction.DESC
            );

            Specification<ProductEntity> spec = new SpecificationBuilder<ProductEntity>()
                    .equal("status", model.getStatus())
                    .likeAnyFieldIgnoreCase(model.getQ(), "productCode")
                    .build();

            Page<ProductResponse> result = productRepository.findAll(spec, pageRequest)
                    .map(productMapper::mapToProductResponse);

            PagedResponse<ProductResponse> response = new PagedResponse<>(
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
            throw new ServiceException(EnumError.PRO_ERR_GET, "product.get.error");
        }
    }

    @Transactional
    @Override
    public ProductResponse uploadImages(UUID productId, List<MultipartFile> files, List<Integer> positions) {

        if (files == null || files.isEmpty()) {
            throw new ServiceException(EnumError.INVALID_REQUEST, "No files uploaded");
        }

        if (files.size() > 3) {
            throw new ServiceException(EnumError.INVALID_REQUEST, "Maximum 3 images allowed");
        }

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ServiceException(EnumError.PRO_ERR_GET, "product.get.error"));

        // Load ảnh hiện tại
        List<ProductImageEntity> currentImages =
                productImageRepository.findByProductIdOrderBySortOrder(productId);

        if (currentImages.size() + files.size() > 3 && (positions == null || positions.isEmpty())) {
            throw new ServiceException(EnumError.INVALID_REQUEST, "Total images cannot exceed 3");
        }

        List<ProductImageEntity> imagesToSave = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {

            MultipartFile file = files.get(i);

            if (file == null || file.isEmpty()) {
                throw new ServiceException(EnumError.INVALID_REQUEST, "File empty");
            }

            String folder = "product/" + productId;
            String uploadedUrl = uploadFileProvider.upload(file, folder);

            int sortOrder;

            // Nếu có truyền positions → replace
            if (positions != null && positions.size() > i) {
                sortOrder = positions.get(i);
                if (sortOrder < 1 || sortOrder > 3) {
                    throw new ServiceException(EnumError.INVALID_REQUEST, "sortOrder must be 1-3");
                }

                // Xóa ảnh cũ tại vị trí đó (nếu có)
                productImageRepository.deleteByProductIdAndSortOrderIn(productId, List.of(sortOrder));

            } else {
                // Nếu không truyền positions → push vào vị trí tiếp theo
                sortOrder = currentImages.size() + 1;
                if (sortOrder > 3) {
                    throw new ServiceException(EnumError.INVALID_REQUEST, "Reached max 3 images");
                }
            }

            ProductImageEntity img = ProductImageEntity.builder()
                    .product(product)
                    .imageUrl(uploadedUrl)
                    .sortOrder(sortOrder)
                    .build();

            imagesToSave.add(img);
        }

        // Lưu các ảnh mới
        productImageRepository.saveAll(imagesToSave);

        // Reorder lại 1–2–3 để khớp tuyệt đối
        reorderImages(productId);

        // Reload product
        product = productRepository.findById(productId).get();

        return productMapper.mapToProductResponse(product);
    }


    @Transactional
    @Override
    public ProductResponse deleteImages(UUID productId, List<Integer> sortOrders) {

        if (sortOrders == null || sortOrders.isEmpty()) {
            throw new ServiceException(EnumError.INVALID_REQUEST);
        }

        List<ProductImageEntity> imagesToDelete =
                productImageRepository.findByProductIdAndSortOrderIn(productId, sortOrders);

        if (imagesToDelete.isEmpty()) {
            throw new ServiceException(EnumError.IMAGE_NOT_FOUND);
        }

        // Xóa ảnh trên Cloudinary (best-effort)
        for (ProductImageEntity img : imagesToDelete) {
            try {
                uploadFileProvider.deleteFileFromCloudinary(img.getImageUrl());
            } catch (Exception e) {
                System.err.println("Failed to delete cloud file: " + img.getImageUrl());
            }
        }

        // Xóa ảnh khỏi DB
        productImageRepository.deleteAll(imagesToDelete);

        // === QUAN TRỌNG ===
        // Đảm bảo Hibernate thật sự delete trong DB
        productImageRepository.flush();

        // Load lại product để không dùng persistence context cũ
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ServiceException(EnumError.PRO_ERR_DEL_EM, "product.get.error"));

        // Lấy danh sách ảnh còn lại từ DB, không dùng product.getImages()
        List<ProductImageEntity> remainingImages =
                productImageRepository.findByProductIdOrderBySortOrder(productId);

        // Reorder lại sortOrder
        int order = 1;
        for (ProductImageEntity img : remainingImages) {
            img.setSortOrder(order++);
        }

        productImageRepository.saveAll(remainingImages);

        return productMapper.mapToProductResponse(product);
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
                    .ratingAverage(productRequest.getRatingAverage())
                    .createdDate(new Date())
                    .build();

            String generatedName = generateNameProduct(productEntity);

            productEntity.setProductName(generatedName);

            productRepository.save(productEntity);

            // redis

            redisService.deleteByKey("products:list:*");

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

        log.info("Calling get by id api with product {}", uuid);

        String key = "product:"+uuid;

        try {
            // get from cache
            ProductResponse cached = redisService.getValue(key, ProductResponse.class);

            if (cached != null) {
                log.info("Redis get for key: {}", key);
                return cached;
            }

            ProductEntity productEntity = productRepository.findById(uuid).orElseThrow(() -> new ServiceException(EnumError.PRO_ERR_GET, "product.get.error", new Object[]{}));

           ProductResponse response = productMapper.mapToProductResponse(productEntity);

            // storge redis
            redisService.setValue(key, response, Constant.CACHE_TTL);

            return  response;
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

            String name = generateNameProduct(productEntity);

            productEntity.setProductName(name);

            //save

            ProductEntity saved = productRepository.save(productEntity);

            // Invalidate cache

            redisService.deleteByKeys("product:" + uuid, "products:list:*");

            return  productMapper.mapToProductResponse(saved);

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

    @Override
    public List<ProductResponse> searchProductSuggestion(String q, int limit) {
        List<ProductEntity> products = productRepository.searchProductSuggestion(q, limit);
        return products.stream().map(pro -> productMapper.mapToProductResponse(pro)).toList();
    }


    // sub funciton
    private void reorderImages(UUID productId) {
        List<ProductImageEntity> imgs = productImageRepository
                .findByProductIdOrderBySortOrder(productId);

        int order = 1;
        for (ProductImageEntity img : imgs) {
            img.setSortOrder(order++);
        }

        productImageRepository.saveAll(imgs);
    }

}
