package com.inventory.repository;

import com.inventory.entity.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {

    List<StockHistory> findByProductCodeOrderByCreatedAtDesc(String productCode);
}
