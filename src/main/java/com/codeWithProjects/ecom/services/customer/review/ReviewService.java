package com.codeWithProjects.ecom.services.customer.review;

import com.codeWithProjects.ecom.dto.OrderedProductsResponseDto;
import com.codeWithProjects.ecom.dto.ReviewDto;

import java.io.IOException;

public interface ReviewService {

    OrderedProductsResponseDto getOrderedProductsDetailsByOrderId(Long orderId);

    Boolean giveReview(ReviewDto reviewDto) throws IOException;
}
