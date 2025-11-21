package com.ecommerce.kientv84.dtos.response;

import com.ecommerce.kientv84.dtos.response.objectRes.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class ProductResponse {
    private UUID id;
    private String productName;
    private String productCode;
    private BrandObjectResponse brand;
    private CategoryObjectResponse category;
    private SubCategoryObjectResponse subCategory;
    private BigDecimal basePrice;
    private String status;
    private String description;
    private CollectionObjectResponse collection;
    private Float discountPercent;
    private String origin;
    private MaterialObjectResponse material;
    private Integer stock;
    private String fitType;
    private String careInstruction;
    private String thumbnailUrl;
    private Double ratingAverage;
    private Integer ratingCount;
    private List<ProductImageResponse> images;
}