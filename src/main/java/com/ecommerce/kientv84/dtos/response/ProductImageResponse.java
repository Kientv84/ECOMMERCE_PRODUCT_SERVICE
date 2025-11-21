package com.ecommerce.kientv84.dtos.response;

import com.ecommerce.kientv84.entites.ProductEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class ProductImageResponse {
    private UUID id;
    private String imageUrl;
    private Integer sortOrder;
}
