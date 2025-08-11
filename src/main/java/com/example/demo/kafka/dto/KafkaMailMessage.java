package com.example.demo.kafka.dto;

import com.microsoft.graph.models.AttachmentInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class KafkaMailMessage extends KafkaMessageDto {
    String messageId;
    String subject;
    String content;
    String from;
    private List<String> to;
    private String receivedDateTime;
    private List<AttachmentInfo> attachments;
}
