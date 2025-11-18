package com.ecommerce.kientv84.dtos.request.search.product;

import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchModel;
import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchOption;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductSearchRequest {
    private ProductSearchOption searchOption;
    private ProductSearchModel searchModel;

    public String hashKey() {
        return "option:" + searchOption.hashKey() + "|filter:" + searchModel.hashKey();
    }
}
