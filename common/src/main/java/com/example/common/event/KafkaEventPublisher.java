package com.example.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Partition anahtarı olmadan gönderim.
     */
    public <T extends EventPayload> void publish(EventType type, T payload) {
        publish(type, null, payload);
    }

    /**
     * Partition anahtarı ile gönderim.
     */
    public <T extends EventPayload> void publish(EventType type, String key, T payload) {
        Objects.requireNonNull(type, "event type is required");
        Objects.requireNonNull(payload, "payload is required");

        Class<? extends EventPayload> expected = type.getPayloadType();
        if (expected != null && !expected.isAssignableFrom(payload.getClass())) {
            throw new IllegalArgumentException(
                    "Invalid payload type for " + type +
                            ". Expected: " + expected.getName() +
                            ", Actual: " + payload.getClass().getName());
        }

        var event = new GenericEvent<>(type, Instant.now(), payload);

        try {
            String json = objectMapper.writeValueAsString(event);

            var record = new ProducerRecord<String, String>(type.getTopics(), key, json);

            record.headers()
                    .add(new RecordHeader("eventType",
                            type.name().getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader("payloadClass",
                            payload.getClass().getName().getBytes(StandardCharsets.UTF_8)));

            kafkaTemplate.send(record).whenComplete((res, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish {} (key={}) to topic {}. payload={}",
                            type, key, type.getTopics(), payload, ex);
                } else {
                    log.info("Published {} (key={}) -> {}-{}@{}",
                            type, key,
                            res.getRecordMetadata().topic(),
                            res.getRecordMetadata().partition(),
                            res.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("Serialization failure for {} (key={}) payload={}", type, key, payload, e);
            throw new RuntimeException(e);
        }
    }
}
