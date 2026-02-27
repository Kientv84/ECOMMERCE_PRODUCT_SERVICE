package com.ecommerce.kientv84.dtos.response.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class KafkaUserResponse {
    private UUID userId;
    private String userName;
    private String userEmail;
}
