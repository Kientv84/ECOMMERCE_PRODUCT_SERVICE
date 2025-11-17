package com.ecommerce.kientv84.dtos.request.search.brand;

import com.ecommerce.kientv84.dtos.request.search.user.UserSearchModel;
import com.ecommerce.kientv84.dtos.request.search.user.UserSearchOption;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BrandSearchRequest {
    private BrandSearchOption searchOption = new BrandSearchOption();
    private BrandSearchModel searchModel = new BrandSearchModel();

    public String hashKey() {
        return "option:" + searchOption.hashKey() + "|filter:" + searchModel.hashKey();
    }
}
