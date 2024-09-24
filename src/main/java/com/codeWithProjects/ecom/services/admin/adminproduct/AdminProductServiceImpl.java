package com.codeWithProjects.ecom.services.admin.adminproduct;

import com.codeWithProjects.ecom.dto.ProductDto;
import com.codeWithProjects.ecom.entity.Category;
import com.codeWithProjects.ecom.entity.Product;
import com.codeWithProjects.ecom.repository.CategoryRepository;
import com.codeWithProjects.ecom.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService{

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    @Cacheable
    public ProductDto addProduct(ProductDto productDto) throws IOException {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setImg(productDto.getImg().getBytes());

        Category category = categoryRepository.findById(productDto.getCategoryId()).orElseThrow();

        product.setCategory(category);
        return productRepository.save(product).getDto();
    }

    @Cacheable
    public List<ProductDto> getAllProducts(){
        List<Product> products = productRepository.findAll();
        return products.stream().map(Product::getDto).collect(Collectors.toList());
    }

    @Cacheable
    public List<ProductDto> getAllProductByName(String name){
        List<Product> products = productRepository.findAllByNameContaining(name);
        return products.stream().map(Product::getDto).collect(Collectors.toList());
    }

    @Cacheable
    @Override
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream()
                .map(Product::getDto) // Convert Product to ProductDto
                .collect(Collectors.toList());
    }

    @Cacheable
    public boolean deleteProduct(Long id){
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()){
            productRepository.deleteById(id);
            return  true;
        }
        return false;
    }

    @Cacheable
    public ProductDto getProductById(Long productId){
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()){
            return optionalProduct.get().getDto();
        }else {
            return null;
        }
    }

    @Cacheable
    public ProductDto updateProduct(Long productId, ProductDto productDto) throws IOException {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        Optional<Category> optionalCategory = categoryRepository.findById(productDto.getCategoryId());
        if (optionalProduct.isPresent() && optionalCategory.isPresent()){
            Product product = optionalProduct.get();

            product.setName(productDto.getName());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());
            product.setCategory(optionalCategory.get());

            if (productDto.getImg() != null){
                product.setImg(productDto.getImg().getBytes());
            }
            return productRepository.save(product).getDto();
        }else {
            return null;
        }
    }
}
