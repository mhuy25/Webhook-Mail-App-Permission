package com.example.demo.kafka.consumer;

import com.example.demo.kafka.topic.KafkaTopic;
import com.example.demo.mongo.dlt.DLTLog;
import com.example.demo.mongo.dlt.DLTLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@Slf4j
@RequiredArgsConstructor
public class DLTConsumer {
    private final ObjectMapper mapper;
    private final DLTLogRepository dltRepo;

    @KafkaListener(topics = KafkaTopic.DLT,
            groupId = "${spring.kafka.consumer.group-id}")
    public void readDlt(ConsumerRecord<String, String> rec) {
        log.error("DLT message, topic={} partition={} offset={}: {}",
                rec.topic(), rec.partition(), rec.offset(), rec.value());
        initDLTLogData(rec);
    }

    private void initDLTLogData(ConsumerRecord<String, String> rec) {
        try {
            DLTLog log = new DLTLog();
            log.setTopic(rec.topic());
            log.setPartition(rec.partition());
            log.setOffset(rec.offset());
            log.setValue(rec.value());
            log.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
            dltRepo.save(log);
        } catch (Exception e) {
            log.info("Kafka#{} listen errors: {}", KafkaTopic.DLT, e.getMessage());
        }
    }
}
