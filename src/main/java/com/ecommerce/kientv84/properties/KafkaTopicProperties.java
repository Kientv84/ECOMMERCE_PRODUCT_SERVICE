package com.ecommerce.kientv84.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties("spring.kafka.product.topic")
public class KafkaTopicProperties {
    private String productCreatedInventory;
    private String errorProduct;
    private String userCreated;
}

