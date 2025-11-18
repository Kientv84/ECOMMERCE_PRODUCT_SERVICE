package com.ecommerce.kientv84.dtos.request.search.category;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategorySearchModel {
    private String q;
    private String status;
    private String categoryCode;

    public String hashKey() {
        return ( q + "-" + status + "-" + categoryCode);
    }
}
