package com.example.demo.mongo.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("mail_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailLog {
    @Id
    private String id;
    private String subject;
    private String content;
    private String from;
    private String to;
    private String cc;
    private String receivedDateTime;
    private LocalDateTime timestamp;
}
