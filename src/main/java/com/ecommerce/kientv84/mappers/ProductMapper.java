package com.ecommerce.kientv84.mappers;

import com.ecommerce.kientv84.dtos.request.kafka.KafkaInventoryRequest;
import com.ecommerce.kientv84.dtos.response.ProductResponse;
import com.ecommerce.kientv84.dtos.response.objectRes.*;
import com.ecommerce.kientv84.entites.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "status", source = "status")
    @Mapping(target = "images", source = "images")
    ProductResponse mapToProductResponse(ProductEntity productEntity);

    BrandObjectResponse mapToProductBrandResponse(BrandEntity brandEntity);

    CategoryObjectResponse mapToProductCategoryResponse(CategoryEntity categoryEntity);

    SubCategoryObjectResponse mapToProductSubCategoryResponse(SubCategoryEntity subCategoryEntity);

    CollectionObjectResponse mapToProductCollectionResponse(CollectionEntity collection);

    MaterialObjectResponse mapToProductMaterialResponse(MaterialEntity materialEntity);

    @Mapping(target = "productId", source = "id")
    KafkaInventoryRequest mapToKafkaInventoryRequest(ProductEntity productEntity);
}
