package com.portfolio.orders.persistence.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SpringDataOrderOutboxRepository
        extends JpaRepository<OrderOutboxEntity, UUID> {

    @Query("""
            select e
            from OrderOutboxEntity e
            where e.publishedAt is null
              and e.attempts < :maxAttempts
              and (e.nextAttemptAt is null or e.nextAttemptAt <= :now)
            order by e.createdAt asc
            """)
    List<OrderOutboxEntity> findEligibleForRetry(
            @Param("now") Instant now,
            @Param("maxAttempts") int maxAttempts);
}
