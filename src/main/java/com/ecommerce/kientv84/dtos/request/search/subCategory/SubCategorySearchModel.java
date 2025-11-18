package com.ecommerce.kientv84.dtos.request.search.subCategory;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubCategorySearchModel {
    private String q;
    private String status;
    private String collectionCode;

    public String hashKey() {
        return ( q + "-" + status + "-" + collectionCode);
    }
}
