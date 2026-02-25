package com.agrosmart.agromarket.repository;

import com.agrosmart.agromarket.enums.ProductCategory;
import com.agrosmart.agromarket.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketRepo extends JpaRepository<Product, Long> {

    List<Product> findAllByIsSoldFalse();

    List<Product> findByCategoryAndIsSoldFalse(ProductCategory category);

    @Query("SELECT p FROM Product p WHERE p.isSold = false AND " +
            "(p.productName LIKE %:search% OR p.description LIKE %:search%)")
    List<Product> searchMarket(@Param("search") String search);

    @Query("SELECT p FROM Product p WHERE p.isSold = false AND p.category = :category AND " +
            "(p.productName LIKE %:search% OR p.description LIKE %:search%)")
    List<Product> findByCategoryAndSearch(@Param("category") ProductCategory category, @Param("search") String search);

    List<Product> findBySellerEmailOrderByCreatedAtDesc(String email);
}