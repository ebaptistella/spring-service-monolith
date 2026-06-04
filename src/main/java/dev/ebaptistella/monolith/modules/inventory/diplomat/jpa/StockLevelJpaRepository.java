package dev.ebaptistella.monolith.modules.inventory.diplomat.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface StockLevelJpaRepository extends JpaRepository<StockLevelEntity, UUID> {
}
