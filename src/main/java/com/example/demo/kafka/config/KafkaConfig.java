package com.example.demo.kafka.config;

import com.example.demo.kafka.topic.KafkaTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {
    /**
     * Container factory cho @KafkaListener:
     * - AckMode MANUAL_IMMEDIATE: commit offset sau khi xử lý thành công (At-least-once)
     * - DefaultErrorHandler: retry 3 lần với delay 1s; sau đó đẩy vào DLT
     * - DLT topic mặc định: <topic gốc>.DLT cùng partition
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaTemplate<Object, Object> kafkaTemplate
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
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
}
