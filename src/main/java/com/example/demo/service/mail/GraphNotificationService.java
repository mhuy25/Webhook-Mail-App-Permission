package com.example.demo.service.mail;

import com.example.demo.config.ErrorConfig;
import com.example.demo.exception.AppException;
import com.example.demo.kafka.dto.KafkaMailMessage;
import com.example.demo.kafka.producer.EventProducer;
import com.example.demo.kafka.topic.KafkaTopic;
import com.example.demo.service.subscription.GraphSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphNotificationService {
    private final EventProducer eventProducer;
    private final GraphSubscriptionService graphSubscriptionService;

    public void SendToKafka(KafkaMailMessage message, String messageId) {
        eventProducer.produceEvent(KafkaTopic.RECEIVED_EMAIL,messageId,message);
    }

    public KafkaMailMessage getMailDetail(String userId, String messageId) {
        try {
            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(graphSubscriptionService.getAccessToken(graphSubscriptionService.getSecretCredential()));
            HttpEntity<?> entity = new HttpEntity<>(headers);

            String url = "https://graph.microsoft.com/v1.0/users/" + userId
                    + "/messages/" + messageId;

            ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = resp.getBody();
            System.out.println(body);
            if (body == null) {
                throw new IllegalStateException("No body data from Graph API for messageId: " + messageId);
            }
            else {
                return getMailDetailFromResource(body);
            }
        } catch (Exception e) {
            throw new AppException(ErrorConfig.FETCH_MAIL_DATA, e.getMessage());
        }
    }

    public String parseUserIdFromResource(String resource) {
        if (resource != null && resource.startsWith("Users/")) {
            String[] arr = resource.split("/");
            if (arr.length >= 2) return arr[1];
        }
        return null;
    }

    public String parseMessageIdFromResource(String resource) {
        if (resource != null && resource.startsWith("Users/")) {
            String[] arr = resource.split("/");
            if (arr.length >= 4 && "Messages".equals(arr[2])) {
                return arr[3];
            }
        }
        return null;
    }

    private KafkaMailMessage getMailDetailFromResource(Map<String, Object> mail) {
        KafkaMailMessage mc = new KafkaMailMessage();
        mc.setSubject((String) mail.get("subject"));
        mc.setContent((String) ((Map)mail.get("body")).get("content"));
        Map<String, Object> from = (Map<String, Object>) mail.get("from");
        if (from != null) {
            Map<String, Object> emailAddress = (Map<String, Object>) from.get("emailAddress");
            if (emailAddress != null) mc.setFrom((String) emailAddress.get("address"));
        }

        List<String> toList = new ArrayList<>();
        List<Map<String, Object>> toRecipients = (List<Map<String, Object>>) mail.get("toRecipients");
        if (toRecipients != null) {
            for (Map<String, Object> r : toRecipients) {
                Map<String, Object> emailAddress = (Map<String, Object>) r.get("emailAddress");
                if (emailAddress != null) toList.add((String) emailAddress.get("address"));
            }
        }
        mc.setTo(toList);
        List<String> ccList = new ArrayList<>();
        List<Map<String, Object>> ccRecipients = (List<Map<String, Object>>) mail.get("ccRecipients");
        if (ccRecipients != null) {
            for (Map<String, Object> r : ccRecipients) {
                Map<String, Object> emailAddress = (Map<String, Object>) r.get("emailAddress");
                if (emailAddress != null) ccList.add((String) emailAddress.get("address"));
            }
        }
        mc.setCc(ccList);
        String rdt = (String) mail.get("receivedDateTime");
        if (rdt != null && !rdt.isBlank()) {
            OffsetDateTime odt = OffsetDateTime.parse(rdt);
            LocalDateTime vnTime = odt.atZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .toLocalDateTime();
            mc.setReceivedDateTime(vnTime.toString());
        }
        return mc;
    }
}
