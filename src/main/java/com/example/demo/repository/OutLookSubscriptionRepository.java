package com.example.demo.repository;

import com.example.demo.entity.OutLookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OutLookSubscriptionRepository extends JpaRepository<OutLookSubscription, Long> {
    Optional<OutLookSubscription> findByEmail(String email);

    Optional<OutLookSubscription> findByResource(String resource);

    Optional<OutLookSubscription> findByIdAndDeletedTimeIsNull(String id);

    Optional<OutLookSubscription> findFirstByEmailAndResourceAndExpiredTimeAfterAndDeletedTimeIsNull(String email, String resource, LocalDateTime expiredTime);
}
