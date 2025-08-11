package com.example.demo.dto.response.microsoft;

import com.example.demo.dto.response.APIResponseDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscribeResponse extends APIResponseDto {
    String id;
    String changeType;
    String resource;
    LocalDateTime expiredTime;
    String clientState;
}
