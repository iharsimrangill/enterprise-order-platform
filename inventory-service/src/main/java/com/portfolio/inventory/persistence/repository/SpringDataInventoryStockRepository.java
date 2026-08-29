package com.portfolio.inventory.persistence.repository;

import com.portfolio.inventory.persistence.entity.InventoryStockEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataInventoryStockRepository extends JpaRepository<InventoryStockEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stock from InventoryStockEntity stock where stock.sku = :sku")
    Optional<InventoryStockEntity> findBySkuForUpdate(@Param("sku") String sku);
}
