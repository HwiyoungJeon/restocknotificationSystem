package com.example.restocknotificationsysyem.domain.repository;

import com.example.restocknotificationsysyem.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p.id FROM Product p")
    List<Long> findAllProductIds();
}
