package com.example.common.event;

import java.time.Instant;

public record GenericEvent<T extends EventPayload>(
        EventType eventType,
        Instant timestamp,
        T payload
) {
}
