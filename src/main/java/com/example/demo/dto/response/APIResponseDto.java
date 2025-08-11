package com.example.demo.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class APIResponseDto {
    String path;
    String status;
    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();
    String message;
}
