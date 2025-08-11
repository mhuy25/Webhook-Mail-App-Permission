package com.example.demo.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GraphApiConfig {
    @Value("${graph.client-id}")
    String clientId;

    @Value("${graph.client-secret}")
    String secretValue;

    @Value("${graph.tenant-id}")
    String tenantId;

    @Value("${graph.webhook-url}")
    String notificationUrl;

    @Value("${graph.lifecycle-url}")
    String lifecycleNotificationUrl;

    @Value("${graph.user.resource}")
    String resource;

    @Value("${graph.user.email}")
    String email;
}
