package com.ecommerce.kientv84.messaging.producers;

import com.ecommerce.kientv84.dtos.request.kafka.KafkaInventoryRequest;
import com.ecommerce.kientv84.dtos.response.kafka.KafkaUserResponse;

public interface UserProducer {
    void produceUserCreate(KafkaUserResponse message);
}
