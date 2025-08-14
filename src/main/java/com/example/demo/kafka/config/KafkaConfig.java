package com.example.demo.kafka.config;

import com.example.demo.kafka.dto.KafkaMailMessage;
import com.example.demo.kafka.topic.KafkaTopic;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topics.received-email.partitions:3}")
    private int receivedEmailPartitions;

    /**
     * Container factory cho @KafkaListener:
     * - AckMode MANUAL_IMMEDIATE: commit offset sau khi xử lý thành công (At-least-once)
     * - DefaultErrorHandler: retry 3 lần với delay 1s; sau đó đẩy vào DLT
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaMailMessage> kafkaListenerContainerFactory(
            ConsumerFactory<String, KafkaMailMessage> consumerFactory,
            KafkaTemplate<String, KafkaMailMessage> kafkaTemplate
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, KafkaMailMessage>();
        factory.setConsumerFactory(consumerFactory);

        // At-least-once: commit tay
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Retry + DLT
        DeadLetterPublishingRecoverer recover = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(KafkaTopic.DLT, record.partition())
        );
        // retry 3 lần, mỗi lần cách 1000ms
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recover, new FixedBackOff(1000L, 3L));

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaMailMessage> DLTListenerContainerFactory(
            ConsumerFactory<String, KafkaMailMessage> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, KafkaMailMessage>();
        factory.setConsumerFactory(consumerFactory);

        // At-least-once: commit tay
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        // retry 3 lần, mỗi lần cách 1000ms
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(0L, 0L));

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public NewTopic receivedEmailTopic() {
        return TopicBuilder.name(KafkaTopic.RECEIVED_EMAIL)
                .partitions(receivedEmailPartitions)
                .replicas(3)
                .configs(Map.of(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2"))
                .build();
    }

    // Topic DLT (cùng partitions & RF)
    @Bean
    public NewTopic receivedEmailDLTTopic() {
        return TopicBuilder.name(KafkaTopic.DLT)
                .partitions(receivedEmailPartitions)
                .replicas(3)
                .configs(Map.of(
                        TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2",
                        TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7L * 24 * 60 * 60 * 1000) // 7 ngày
                ))
                .build();
    }
}
