package com.codeWithProjects.ecom.services.customer;

import com.codeWithProjects.ecom.dto.ProductDetailDto;
import com.codeWithProjects.ecom.dto.ProductDto;
import com.codeWithProjects.ecom.entity.FAQ;
import com.codeWithProjects.ecom.entity.Product;
import com.codeWithProjects.ecom.entity.Review;
import com.codeWithProjects.ecom.repository.FAQRepository;
import com.codeWithProjects.ecom.repository.ProductRepository;
import com.codeWithProjects.ecom.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerProductServiceImpl implements CustomerProductService{

    private final ProductRepository productRepository;

    private final FAQRepository faqRepository;

    private final ReviewRepository reviewRepository;

    @Cacheable
    public List<ProductDto> getAllProducts(){
        List<Product> products = productRepository.findAll();
        return products.stream().map(Product::getDto).collect(Collectors.toList());
    }

    @Cacheable
    public List<ProductDto> searchProductByTitle(String name){
        List<Product> products = productRepository.findAllByNameContaining(name);
        return products.stream().map(Product::getDto).collect(Collectors.toList());
    }

    @Cacheable
    public ProductDetailDto getProductDetailById(Long productId){
        Optional<Product> optionalProduct = productRepository.findById(productId);

        if (optionalProduct.isPresent()){
            Product product = optionalProduct.get();

            List<FAQ> faqList = faqRepository.findAllByProductId(productId);
            List<Review> reviewList = reviewRepository.findAllByProductId(productId);

            ProductDetailDto productDetailDto = new ProductDetailDto();
            productDetailDto.setProductDto(product.getDto());
            productDetailDto.setFaqDtoList(faqList.stream().map(FAQ::getFAQDto).collect(Collectors.toList()));
            productDetailDto.setReviewDtoList(reviewList.stream().map(Review::getDto).collect(Collectors.toList()));

            return productDetailDto;
        }
        return null;
    }

    @Cacheable
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream()
                .map(Product::getDto) // Convert Product to ProductDto
                .collect(Collectors.toList());
    }

    @Cacheable
    public List<ProductDto> getFilteredProducts(Long categoryId, Double minPrice, Double maxPrice, Boolean isAvailable) {
        List<Product> products = productRepository.findByCategoryIdAndPriceBetweenAndAvailability(categoryId, minPrice, maxPrice, isAvailable);
        return products.stream().map(Product::getDto).collect(Collectors.toList());
    }
}
