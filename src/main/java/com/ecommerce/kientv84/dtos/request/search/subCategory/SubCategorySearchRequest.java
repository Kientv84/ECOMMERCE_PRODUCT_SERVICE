package com.ecommerce.kientv84.dtos.request.search.subCategory;

import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchModel;
import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchOption;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubCategorySearchRequest {
    private SubCategorySearchOption searchOption;
    private SubCategorySearchModel searchModel;

    public String hashKey() {
        return "option:" + searchOption.hashKey() + "|filter:" + searchModel.hashKey();
    }
}
