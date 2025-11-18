package com.ecommerce.kientv84.dtos.request.search.role;

import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchModel;
import com.ecommerce.kientv84.dtos.request.search.collection.CollectionSearchOption;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleSearchRequest {
    private RoleSearchOption searchOption;
    private RoleSearchModel searchModel;

    public String hashKey() {
        return "option:" + searchOption.hashKey() + "|filter:" + searchModel.hashKey();
    }
}
