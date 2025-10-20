package com.example.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class AuditConfig {

    @Bean
    public Clock utcClock() { return Clock.systemUTC(); }

    @Bean
    public DateTimeProvider auditingDateTimeProvider(Clock utcClock) {
        return () -> Optional.of(Instant.now(utcClock));
    }
}