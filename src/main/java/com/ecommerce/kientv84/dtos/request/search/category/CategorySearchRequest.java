package com.ecommerce.kientv84.dtos.request.search.category;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategorySearchRequest {
    private CategorySearchOption searchOption = new CategorySearchOption();
    private CategorySearchModel searchModel = new CategorySearchModel();

    public String hashKey() {
        return "option:" + searchOption.hashKey() + "|filter:" + searchModel.hashKey();
    }
}
