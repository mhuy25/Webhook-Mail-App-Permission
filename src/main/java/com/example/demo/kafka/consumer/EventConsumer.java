package com.example.demo.kafka.consumer;

import com.example.demo.kafka.topic.KafkaTopic;
import com.example.demo.mongo.mail.MailLog;
import com.example.demo.mongo.mail.MailLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {
    private final MailLogRepository mailRepo;
    private final ObjectMapper mapper;

    @KafkaListener(topics = {KafkaTopic.RECEIVED_EMAIL,},
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeAuthEvent(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic ,
                                 ConsumerRecord<String, String> record,
                                 Acknowledgment ack) {
        try {
            initAuthLogData(record.value());
            ack.acknowledge();
        } catch (Exception e) {
            errorMessage(topic, e);
        }
    }

    private void initAuthLogData(String message) throws JsonProcessingException {
        JsonNode json = mapper.readTree(message);
        MailLog log = new MailLog();
        log.setContent(json.get("content").asText());
        log.setSubject(json.get("subject").asText());
        log.setFrom(json.get("from").asText());
        JsonNode toNode = json.path("to");
        if (toNode.isArray()) {
            List<String> to = new ArrayList<>();
            toNode.forEach(n -> to.add(n.asText()));
            log.setTo(String.join(",", to));
        } else {
            log.setTo(toNode.asText());
        }
        log.setReceivedDateTime(json.get("receivedDateTime").asText());
        log.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
        mailRepo.save(log);
    }

    private void errorMessage(String event, Exception e) {
        log.info("Kafka#{} listen errors: {}", event, e.getMessage());
        throw new RuntimeException(e);
    }
}
