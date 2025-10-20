package com.example.notification.serviceImpl;

import com.example.notification.service.IEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService implements IEmailService {
    @Override
    public void send(String to, String subject, String body) {
        log.info("📧 [SIMULATED EMAIL] to={} | subject={} | body=\n{}", to, subject, body);
    }
}
