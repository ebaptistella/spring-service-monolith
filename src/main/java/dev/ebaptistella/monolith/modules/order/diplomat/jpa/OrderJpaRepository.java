package dev.ebaptistella.monolith.modules.order.diplomat.jpa;

import dev.ebaptistella.monolith.modules.order.models.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    @EntityGraph(attributePaths = "lines")
    Optional<OrderEntity> findById(UUID id);

    @EntityGraph(attributePaths = "lines")
    Optional<OrderEntity> findByIdempotencyKey(UUID idempotencyKey);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0)
            FROM OrderEntity o
            WHERE o.customerId = :customerId AND o.status = :status
            """)
    BigDecimal sumTotalAmountByCustomerIdAndStatus(
            @Param("customerId") UUID customerId, @Param("status") OrderStatus status);
}
