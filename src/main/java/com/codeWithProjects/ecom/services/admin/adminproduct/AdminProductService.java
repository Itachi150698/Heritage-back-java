package com.codeWithProjects.ecom.services.admin.adminproduct;

import com.codeWithProjects.ecom.dto.ProductDto;
import com.codeWithProjects.ecom.entity.Product;

import java.io.IOException;
import java.util.List;

public interface AdminProductService {

    ProductDto addProduct(ProductDto productDto) throws IOException;

    List<ProductDto> getAllProducts();

    List<ProductDto> getAllProductByName(String name);

    List<ProductDto> getProductsByCategory(Long categoryId);

    boolean deleteProduct(Long id);

    ProductDto getProductById(Long productId);

    ProductDto updateProduct(Long productId, ProductDto productDto) throws IOException;

}
