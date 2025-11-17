package com.ecommerce.kientv84.dtos.request.search.brand;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BrandSearchModel {
    private String q;
    private String status;
    private String brandCode;

    public String hashKey() {
        return ( q + "-" + status + "-" + brandCode);
    }
}
