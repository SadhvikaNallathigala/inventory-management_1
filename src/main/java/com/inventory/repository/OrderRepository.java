package com.inventory.repository;

import com.inventory.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findByOrderCode(String orderCode);

    boolean existsByOrderCode(String orderCode);

    long countByPremiumTrue();

    long countByPremiumFalse();

    @Query("select o from CustomerOrder o where " +
            "(cast(:customerName as string) is null or lower(o.customerName) like lower(concat('%', cast(:customerName as string), '%'))) " +
            "and (:isPremium is null or o.premium = :isPremium) " +
            "order by o.createdAt desc")
    List<CustomerOrder> search(@Param("customerName") String customerName,
                               @Param("isPremium") Boolean isPremium);
}