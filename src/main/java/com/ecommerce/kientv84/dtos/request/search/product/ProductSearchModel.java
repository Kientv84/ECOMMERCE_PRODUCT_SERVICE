package com.ecommerce.kientv84.dtos.request.search.product;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductSearchModel {
    private String q;
    private String status;
    private String productCode;

    public String hashKey() {
        return ( q + "-" + status + "-" + productCode);
    }
}
