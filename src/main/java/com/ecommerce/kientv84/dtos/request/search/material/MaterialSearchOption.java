package com.ecommerce.kientv84.dtos.request.search.material;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaterialSearchOption {
    private Integer page = 0;
    private Integer size = 10;
    private String sort = "createDate,desc";

    public String hashKey() {
        return page + "-" + size + "-" + sort;
    }
}
