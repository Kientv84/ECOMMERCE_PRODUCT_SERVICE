package com.ecommerce.kientv84.dtos.request.kafka;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventMetadata {
    private UUID eventId;
    private String eventType;
    private String source;
    private int version;              
}

