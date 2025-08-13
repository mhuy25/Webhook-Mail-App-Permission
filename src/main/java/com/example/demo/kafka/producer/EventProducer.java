package com.example.demo.kafka.producer;

import com.example.demo.kafka.dto.KafkaMailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventProducer {
    private final KafkaTemplate<String, KafkaMailMessage> kafkaTemplate;

    public void produceEvent(String topic, String key, KafkaMailMessage message) {
        kafkaTemplate.send(topic, key, message).whenComplete((res, ex) -> {
            if (ex != null) {
                log.error("Kafka send FAILED topic={} key={} err={}", topic, key, ex.toString(), ex);
            } else {
                var m = res.getRecordMetadata();
                log.info("Kafka sent OK topic={} key={} partition={} offset={}", m.topic(), key, m.partition(), m.offset());
            }
        });
    }
}

