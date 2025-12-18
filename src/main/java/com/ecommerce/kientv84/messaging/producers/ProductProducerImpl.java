package com.ecommerce.kientv84.messaging.producers;

import com.ecommerce.kientv84.dtos.request.kafka.EventMetadata;
import com.ecommerce.kientv84.dtos.request.kafka.KafkaEvent;
import com.ecommerce.kientv84.dtos.request.kafka.KafkaInventoryRequest;
import com.ecommerce.kientv84.properties.KafkaTopicProperties;
import com.ecommerce.kientv84.services.KafkaService;
import com.ecommerce.kientv84.utils.KafkaObjectError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductProducerImpl implements ProductProducer{
    private final KafkaTopicProperties kafkaTopicProperties;
    private final KafkaService kafkaService;

    @Override
    public void produceInventoryCreate(KafkaInventoryRequest kafkaInventoryRequest) {
        var topic = kafkaTopicProperties.getProductCreatedInventory();
        log.info("[produceInventoryCreate] producing error to topic {}", topic);

        KafkaEvent<KafkaInventoryRequest> message = KafkaEvent.<KafkaInventoryRequest>builder()
                .metadata(EventMetadata.builder()
                        .eventId(UUID.randomUUID())
                        .eventType(topic)
                        .source("product-service")
                        .version(1)
                        .build())
                .payload(kafkaInventoryRequest)
                .build();

        kafkaService.send(topic, message);
    }

    @Override
    public void produceMessageError(KafkaObjectError kafkaObject) {
        var topic = kafkaTopicProperties.getErrorProduct();
        log.info("[produceMessageError] producing error to topic {}", topic);
        kafkaService.send(topic, kafkaObject);
    }
}
