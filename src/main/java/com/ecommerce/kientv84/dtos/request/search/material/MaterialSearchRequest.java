package com.ecommerce.kientv84.dtos.request.search.material;

import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchModel;
import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchOption;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaterialSearchRequest {
    private MaterialSearchOption searchOption;
    private MaterialSearchRequest searchModel;

    public String hashKey() {
        return "option:" + searchOption.hashKey() + "|filter:" + searchModel.hashKey();
    }
}
