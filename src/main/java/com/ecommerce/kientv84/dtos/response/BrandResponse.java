package com.ecommerce.kientv84.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Setter
@Getter
public class BrandResponse {
    private UUID id;
    private String brandName;
    private String brandCode;
    private String status;
    private String description;
    private String thumbnailUrl;
}

