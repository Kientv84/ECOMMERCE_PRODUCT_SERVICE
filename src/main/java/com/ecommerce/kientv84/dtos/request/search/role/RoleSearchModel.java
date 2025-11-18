package com.ecommerce.kientv84.dtos.request.search.role;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleSearchModel {
    private String q;
    private String status;

    public String hashKey() {
        return ( q + "-" + status);
    }
}
