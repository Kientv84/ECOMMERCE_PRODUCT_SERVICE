package com.ecommerce.kientv84.dtos.request.search.collection;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CollectionSearchModel {
    private String q;
    private String status;
    private String collectionCode;

    public String hashKey() {
        return ( q + "-" + status + "-" + collectionCode);
    }
}
