package com.example.demo.kafka.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentInfo {
    private String name;
    private String contentType;
    private Long size;
    private String contentId;
}
