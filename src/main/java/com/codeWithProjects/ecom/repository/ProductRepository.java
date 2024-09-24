package com.codeWithProjects.ecom.repository;

import com.codeWithProjects.ecom.dto.ProductDto;
import com.codeWithProjects.ecom.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByNameContaining(String title);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByCategoryIdAndPriceBetweenAndAvailability(Long categoryId, Double minPrice, Double maxPrice, Boolean isAvailable);
}
