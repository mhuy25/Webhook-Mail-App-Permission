package com.example.demo.controller;

import com.example.demo.dto.request.microsoft.NotificationRequest;
import com.example.demo.kafka.dto.KafkaMailMessage;
import com.example.demo.service.mail.GraphNotificationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhook")
public class WebhookController extends RestfulController{
    private final GraphNotificationService graphNotificationService;

    @PostMapping(value = "/outlook", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> validate(@RequestParam("validationToken") String token) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(token);
    }

    @PostMapping("/outlook")
    public void onNotification(@RequestBody NotificationRequest notificationRequest, HttpServletResponse response) {
        try {
            if (notificationRequest != null && notificationRequest.getValue() != null) {
                for (NotificationRequest.NotificationValue notification : notificationRequest.getValue()) {
                    String userId = graphNotificationService.parseUserIdFromResource(notification.getResource());
                    String messageId = graphNotificationService.parseMessageIdFromResource(notification.getResource());
                    KafkaMailMessage content = graphNotificationService.getMailDetail(userId, messageId);
                    graphNotificationService.SendToKafka(content, messageId);
                }
                response.setStatus(200);
            }
        } catch (Exception e) {
            log.error("Webhook xử lý lỗi: ", e);
            response.setStatus(500);
        }
    }
}

