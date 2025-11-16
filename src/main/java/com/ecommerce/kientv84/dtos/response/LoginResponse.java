package com.ecommerce.kientv84.dtos.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private int success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String name;

    public LoginResponse(int success, String message) {
        this.message = message;
        this.success = success;
    }
}
