package com.ecommerce.kientv84.mappers;

import com.ecommerce.kientv84.dtos.response.ProductImageResponse;
import com.ecommerce.kientv84.entites.ProductImageEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {
    ProductImageResponse mapToProductImageResponse(ProductImageEntity productImageEntity);
}
