package com.ecommerce.kientv84.dtos.request;

import lombok.Data;

import java.util.List;

@Data
public class DeleteImagesRequest {
    private List<Integer> sortOrders;
}
