package com.codeWithProjects.ecom.services.customer;

import com.codeWithProjects.ecom.dto.ProductDetailDto;
import com.codeWithProjects.ecom.dto.ProductDto;

import java.util.List;

public interface CustomerProductService {

    List<ProductDto> searchProductByTitle(String title);

    List<ProductDto> getAllProducts();

    ProductDetailDto getProductDetailById(Long productId);

    List<ProductDto> getProductsByCategory(Long categoryId);

    List<ProductDto> getFilteredProducts(Long categoryId, Double minPrice, Double maxPrice, Boolean isAvailable);

}
