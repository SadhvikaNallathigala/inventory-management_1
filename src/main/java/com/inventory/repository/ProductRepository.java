package com.inventory.repository;

import com.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByCode(String code);

    Optional<Product> findByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    @Query("select p from Product p where p.deleted = false " +
            "and (cast(:keyword as string) is null or lower(p.name) like lower(concat('%', cast(:keyword as string), '%')) " +
            "     or lower(p.code) like lower(concat('%', cast(:keyword as string), '%'))) " +
            "and (cast(:category as string) is null or lower(p.category) = lower(cast(:category as string))) " +
            "order by p.code asc")
    List<Product> search(@Param("keyword") String keyword, @Param("category") String category);
}