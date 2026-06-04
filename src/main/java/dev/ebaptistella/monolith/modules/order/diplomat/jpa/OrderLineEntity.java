package dev.ebaptistella.monolith.modules.order.diplomat.jpa;

import dev.ebaptistella.monolith.modules.order.models.OrderLine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_lines")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderLineEntity {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    static OrderLineEntity fromModel(OrderLine line, OrderEntity order) {
        OrderLineEntity entity = new OrderLineEntity();
        entity.id = line.id();
        entity.order = order;
        entity.skuId = line.skuId();
        entity.quantity = line.quantity();
        entity.unitPrice = line.unitPrice();
        entity.lineTotal = line.lineTotal();
        return entity;
    }

    OrderLine toModel() {
        return new OrderLine(id, order.getId(), skuId, quantity, unitPrice, lineTotal);
    }
}
