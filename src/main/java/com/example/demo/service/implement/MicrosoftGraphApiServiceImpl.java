package com.example.demo.service.implement;

import com.example.demo.dto.response.microsoft.SubscribeResponse;
import com.microsoft.graph.models.Subscription;

public interface MicrosoftGraphApiServiceImpl {
    SubscribeResponse createNewSubscription();

    SubscribeResponse renewSubscription(String subscriptionId);

    void deleteSubscription(String subscriptionId);
}
