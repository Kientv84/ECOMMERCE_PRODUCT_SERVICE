package com.ecommerce.kientv84.messaging.producers;

import com.ecommerce.kientv84.dtos.request.kafka.KafkaInventoryRequest;
import com.ecommerce.kientv84.dtos.response.kafka.KafkaUserResponse;
import com.ecommerce.kientv84.utils.KafkaObjectError;

public interface ProductProducer {
    void produceInventoryCreate(KafkaInventoryRequest message);

    void produceMessageError(KafkaObjectError kafkaObject);
}
