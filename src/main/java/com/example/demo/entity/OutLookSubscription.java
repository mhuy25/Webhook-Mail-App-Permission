package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@Table(name = "outlook_subscribe")
public class OutLookSubscription {
    @Id
    String id;
    String email;
    String resource;
    LocalDateTime expiredTime;
    LocalDateTime createdTime;
    LocalDateTime updatedTime;
    LocalDateTime deletedTime;
    String changeType;
    String clientState;
}
