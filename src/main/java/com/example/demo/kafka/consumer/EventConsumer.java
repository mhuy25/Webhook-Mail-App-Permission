package com.example.demo.kafka.consumer;

import com.example.demo.kafka.dto.KafkaMailMessage;
import com.example.demo.kafka.topic.KafkaTopic;
import com.example.demo.mongo.mail.MailLog;
import com.example.demo.mongo.mail.MailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {
    private final MailLogRepository mailRepo;

    @KafkaListener(topics = {KafkaTopic.RECEIVED_EMAIL,},
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeAuthEvent(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic ,
                                 KafkaMailMessage msg,
                                 Acknowledgment ack) {
        try {
            initAuthLogData(msg);
            ack.acknowledge();
        } catch (Exception e) {
            errorMessage(topic, e);
        }
    }

    private void initAuthLogData(KafkaMailMessage message) {
        try {
            MailLog log = new MailLog();
            log.setContent(message.getContent());
            log.setSubject(message.getSubject());
            log.setFrom(message.getFrom());
            log.setTo(String.join(",", message.getTo()));
            log.setCc(String.join(",", message.getCc()));
            log.setReceivedDateTime(message.getReceivedDateTime());
            log.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
            mailRepo.save(log);
        } catch (Exception e) {
            errorMessage(KafkaTopic.RECEIVED_EMAIL, e);
        }
    }

    private void errorMessage(String event, Exception e) {
        log.info("Kafka#{} listen errors: {}", event, e.getMessage());
        throw new RuntimeException(e);
    }
}
