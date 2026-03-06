package com.example.Project2.repository;

import com.example.Project2.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product , Integer> {

    // Tìm kiếm theo keyword
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // Lọc theo categoryId
    Page<Product> findByCategory_Id(Integer categoryId, Pageable pageable);
}
