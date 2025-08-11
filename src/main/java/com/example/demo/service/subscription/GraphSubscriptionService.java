package com.example.demo.service.subscription;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.example.demo.config.ErrorConfig;
import com.example.demo.config.GraphApiConfig;
import com.example.demo.dto.response.microsoft.SubscribeResponse;
import com.example.demo.entity.OutLookSubscription;
import com.example.demo.exception.AppException;
import com.example.demo.mapper.SubscribeResponseMapper;
import com.example.demo.repository.OutLookSubscriptionRepository;
import com.example.demo.service.implement.MicrosoftGraphApiServiceImpl;
import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class GraphSubscriptionService implements MicrosoftGraphApiServiceImpl {
    private final OutLookSubscriptionRepository SubscriptionRepo;
    private final GraphApiConfig graphApiConfig;
    private final SubscribeResponseMapper subscribeResponseMapper;
    private final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public SubscribeResponse createNewSubscription() {
        try {
            OutLookSubscription subscription = SubscriptionRepo.findFirstByEmailAndResourceAndExpiredTimeAfterAndDeletedTimeIsNull(
                    graphApiConfig.getEmail(),
                    graphApiConfig.getResource(),
                    LocalDateTime.now()
            ).orElse(null);
            if (subscription == null) {
                Subscription newSubscription = createSubscription();
                subscription = saveNewSubscription(newSubscription);
            }
            log.info("Subscription created: {}", subscription);
            return subscribeResponseMapper.toSubscribeResponse(subscription);
        } catch (Exception e) {
            throw new AppException(ErrorConfig.CREATE_NEW_SUBSCRIPTION,e.getMessage());
        }
    }

    @Override
    public void deleteSubscription(String id) {
        try {
            OutLookSubscription outLookSubscription = getSubscriptionFromDB(id);
            GraphServiceClient graphClient = getGraphServiceClient();
            graphClient.subscriptions().bySubscriptionId(outLookSubscription.getId()).delete();
            softDeleteSubscriptionDB(outLookSubscription);
        } catch (Exception e) {
            throw new AppException(ErrorConfig.SUBSCRIPTION_DELETE_FAILED,e.getMessage());
        }
    }

    @Override
    public SubscribeResponse renewSubscription(String id) {
        try {
            OutLookSubscription outLookSubscription = getSubscriptionFromDB(id);
            GraphServiceClient graphClient = getGraphServiceClient();
            Subscription subscription = new Subscription();
            subscription.setExpirationDateTime(OffsetDateTime.now().plusMinutes(30));
            subscription = graphClient.subscriptions().bySubscriptionId(outLookSubscription.getId()).patch(subscription);
            OutLookSubscription updatedSubscription = renewSubscriptionDB(outLookSubscription,
                    subscription.getExpirationDateTime().atZoneSameInstant(VN).toLocalDateTime());
            log.info("Subscription Renew : {}", outLookSubscription.getId());
            return subscribeResponseMapper.toSubscribeResponse(updatedSubscription);
        } catch (Exception e) {
            log.error(ErrorConfig.RENEW_SUBSCRIPTION, e.getMessage());
            return null;
        }
    }

    private OutLookSubscription saveNewSubscription(Subscription subscription) {
        try {
            return SubscriptionRepo.save(OutLookSubscription.builder()
                    .changeType(subscription.getChangeType())
                    .resource(subscription.getResource())
                    .createdTime(LocalDateTime.now())
                    .id(subscription.getId())
                    .email(graphApiConfig.getEmail())
                    .clientState(subscription.getClientState())
                    .expiredTime(subscription.getExpirationDateTime().atZoneSameInstant(VN).toLocalDateTime())
                    .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorConfig.SUBSCRIPTION_SAVE_FAILED,e.getMessage());
        }
    }

    private Subscription createSubscription() {
        try {
            GraphServiceClient graphClient = getGraphServiceClient();
            Subscription subscription = initNewSubscription();
            return graphClient.subscriptions().post(subscription);
        } catch (com.microsoft.kiota.ApiException ex) {
            log.error("Create subscription failed. Status={}, Headers={}, Message={}",
                    ex.getResponseStatusCode(),
                    ex.getResponseHeaders(),
                    ex.getMessage()
            );
            throw ex;
        }
    }

    private GraphServiceClient getGraphServiceClient() {
        if (graphApiConfig.getClientId() == null || graphApiConfig.getTenantId() == null || graphApiConfig.getSecretValue() == null) {
            throw new AppException(ErrorConfig.GET_GRAPH_SERVICE_CLIENT, "Invalid Graph API Config.");
        }

        ClientSecretCredential credential = getSecretCredential();
        String[] scopes = new String[] { "https://graph.microsoft.com/.default" };

        return new GraphServiceClient(credential, scopes);
    }

    public ClientSecretCredential getSecretCredential() {
        return new ClientSecretCredentialBuilder()
                .clientId(graphApiConfig.getClientId())
                .tenantId(graphApiConfig.getTenantId())
                .clientSecret(graphApiConfig.getSecretValue())
                .build();
    }

    public String getAccessToken(ClientSecretCredential credential) {
        var token = credential.getToken(
                new TokenRequestContext().addScopes("https://graph.microsoft.com/.default")
        ).block();
        if (token == null) {
            throw new IllegalStateException("Cannot acquire Graph access token");
        }
        return token.getToken();
    }

    private Subscription initNewSubscription() {
        Subscription subscription = new Subscription();
        subscription.setChangeType("created");
        subscription.setNotificationUrl(graphApiConfig.getNotificationUrl());
        subscription.setLifecycleNotificationUrl(graphApiConfig.getLifecycleNotificationUrl());
        subscription.setResource(graphApiConfig.getResource());
        OffsetDateTime expiration = OffsetDateTime.now().plusMinutes(30);
        subscription.setExpirationDateTime(expiration);
        subscription.setClientState(UUID.randomUUID().toString());
        return subscription;
    }

    private void softDeleteSubscriptionDB(OutLookSubscription subscription) {
        subscription.setDeletedTime(LocalDateTime.now());
        SubscriptionRepo.save(subscription);
    }

    public OutLookSubscription getSubscriptionFromDB(String id) {
        return SubscriptionRepo.findByIdAndDeletedTimeIsNull(id)
                .orElseThrow(() -> new AppException(ErrorConfig.SUBSCRIPTION_NOT_FOUND,"Subscription not found."));
    }

    private OutLookSubscription renewSubscriptionDB(OutLookSubscription subscription, LocalDateTime newExpiredTime) {
        subscription.setUpdatedTime(LocalDateTime.now());
        subscription.setExpiredTime(newExpiredTime);
        return SubscriptionRepo.save(subscription);
    }
}
