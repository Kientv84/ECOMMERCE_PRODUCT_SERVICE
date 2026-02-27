package com.ecommerce.kientv84.messaging.producers;

import com.ecommerce.kientv84.commons.EventType;
import com.ecommerce.kientv84.dtos.request.kafka.EventMetadata;
import com.ecommerce.kientv84.dtos.request.kafka.KafkaEvent;
import com.ecommerce.kientv84.dtos.response.kafka.KafkaUserResponse;
import com.ecommerce.kientv84.properties.KafkaTopicProperties;
import com.ecommerce.kientv84.services.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProducerImpl implements  UserProducer{
    private final KafkaTopicProperties kafkaTopicProperties;
    private final KafkaService kafkaService;


    @Override
    public void produceUserCreate(KafkaUserResponse message) {
        var topic = kafkaTopicProperties.getUserCreated();

        KafkaEvent event = KafkaEvent.builder()
                .metadata(EventMetadata.builder()
                        .eventId(UUID.randomUUID())
                        .eventType(EventType.USER_CREATE.name())
                        .source("product-user-service")
                        .version(1)
                        .build())
                .payload(message)
                .build();

        kafkaService.send(topic, event);
    }
}
