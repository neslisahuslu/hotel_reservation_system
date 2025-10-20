package com.example.notification.service;

public interface IEmailService {
    void send(String to, String subject, String body);
}