package com.ecommerce.kientv84.dtos.request.search.material;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaterialSearchModel {
    private String q;
    private String status;
    private String materialCode;

    public String hashKey() {
        return ( q + "-" + status + "-" + materialCode);
    }
}
