package com.codeWithProjects.ecom.services.customer.wishlist;

import com.codeWithProjects.ecom.dto.WishlistDto;

import java.util.List;

public interface WishlistService {

    WishlistDto addProductToWishlist(WishlistDto wishlistDto);

    List<WishlistDto> getWishlistByUserId(Long userId);

    boolean removeProductFromWishlist(Long userId, Long productId);
}
