package com.example.demo.kafka.consumer;

import com.example.demo.kafka.dto.KafkaMailMessage;
import com.example.demo.kafka.topic.KafkaTopic;
import com.example.demo.mongo.dlt.DLTLog;
import com.example.demo.mongo.dlt.DLTLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@Slf4j
@RequiredArgsConstructor
public class DLTConsumer {
    private final DLTLogRepository dltRepo;

    @KafkaListener(topics = KafkaTopic.DLT,
            groupId = "dlt-log",
            containerFactory = "DLTListenerContainerFactory")
    public void readDlt(ConsumerRecord<String, KafkaMailMessage> rec, Acknowledgment ack) {
        log.error("DLT message, topic={} partition={} offset={}: {}",
                rec.topic(), rec.partition(), rec.offset(), rec.value());
        initDLTLogData(rec);
        ack.acknowledge();
    }

    private void initDLTLogData(ConsumerRecord<String, KafkaMailMessage> rec) {
        try {
            DLTLog log = new DLTLog();
            log.setTopic(rec.topic());
            log.setPartition(rec.partition());
            log.setOffset(rec.offset());
            log.setValue(rec.value().getContent());
            log.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
            dltRepo.save(log);
        } catch (Exception e) {
            log.info("Kafka#{} listen errors: {}", KafkaTopic.DLT, e.getMessage());
        }
    }
}
