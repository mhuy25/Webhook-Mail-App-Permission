package com.example.demo.dto.request.microsoft;

import com.example.demo.dto.request.APIRequestDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class NotificationRequest extends APIRequestDto {
    private List<NotificationValue> value;

    @Data
    @NoArgsConstructor
    public static class NotificationValue {
        private String subscriptionId;
        private String changeType;
        private String resource;
        private String tenantId;
        private String clientState;
        private String id;
    }
}
