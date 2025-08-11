package com.example.demo.controller;

import com.example.demo.dto.request.microsoft.NotificationRequest;
import com.example.demo.dto.response.APIResponse;
import com.example.demo.entity.OutLookSubscription;
import com.example.demo.service.subscription.GraphSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscribe")
@RequiredArgsConstructor
@Slf4j
public class SubscribeController extends RestfulController {
    private final GraphSubscriptionService graphSubscriptionService;

    @PostMapping("/subscribe")
    ResponseEntity<APIResponse> subscribe() {
        try {
            return ok(graphSubscriptionService.createNewSubscription());
        }
        catch (Exception e) {
            log.error("Can not create subscription: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.builder().message("Can not create subscription: " + e.getMessage()).build());
        }
    }

    @PostMapping("/{id}/renew")
    ResponseEntity<APIResponse> renew(@PathVariable String id) {
        try {
            OutLookSubscription outLookSubscription = graphSubscriptionService.getSubscriptionFromDB(id);
            return ok(graphSubscriptionService.renewSubscription(outLookSubscription.getId()));
        }
        catch (Exception e) {
            log.error("Can not renew subscription: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.builder().message("Can not renew subscription: " + e.getMessage()).build());
        }
    }

    @DeleteMapping("/{id}")
    ResponseEntity<APIResponse> delete(@PathVariable String id) {
        try {
            graphSubscriptionService.deleteSubscription(id);
            return ok("Deleted");
        }
        catch (Exception e) {
            log.error("Can not delete subscription: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.builder().message("Can not delete subscription: " + e.getMessage()).build());
        }
    }

    @PostMapping(value = "/lifecycle", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> validateLifeCycle(@RequestParam("validationToken") String token) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(token);
    }

    @PostMapping("/lifecycle")
    public ResponseEntity<?> onLifecycle(@RequestBody NotificationRequest lifeCycleRequest) {
        try {
            if (lifeCycleRequest == null || lifeCycleRequest.getValue() == null || lifeCycleRequest.getValue().isEmpty()) {
                return ResponseEntity.accepted().build();
            }
            else {
                for (NotificationRequest.NotificationValue notification : lifeCycleRequest.getValue()) {
                    String subscriptionId = notification.getSubscriptionId();
                    graphSubscriptionService.renewSubscription(subscriptionId);
                }
                return ResponseEntity.ok().build();
            }
        }
        catch (Exception e) {
            log.error("Can not renew subscription: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Can not renew subscription: " + e.getMessage());
        }
    }
}