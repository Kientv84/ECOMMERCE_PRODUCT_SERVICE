package com.ecommerce.kientv84.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String userName;
    private String status;
    private String userEmail;
    private String userPhoneNumber;
    private String userAvatar;
}
