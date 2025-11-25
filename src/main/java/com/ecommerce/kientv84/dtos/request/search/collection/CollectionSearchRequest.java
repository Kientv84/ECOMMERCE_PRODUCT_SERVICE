package com.ecommerce.kientv84.dtos.request.search.collection;

import com.ecommerce.kientv84.dtos.request.search.category.CategorySearchModel;
import com.ecommerce.kientv84.dtos.request.search.category.CategorySearchOption;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CollectionSearchRequest {
    private CollectionSearchOption searchOption = new CollectionSearchOption();
    private CollectionSearchModel searchModel = new CollectionSearchModel();

    public String hashKey() {
        return "option:" + searchOption.hashKey() + "|filter:" + searchModel.hashKey();
    }
}
