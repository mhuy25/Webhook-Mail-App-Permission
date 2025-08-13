package com.example.demo.mongo.dlt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("dlt_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DLTLog {
    @Id
    private String id;
    private String topic;
    private Integer partition;
    private Long offset;
    private LocalDateTime timestamp;
    private String value;
}
