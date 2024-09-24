package com.codeWithProjects.ecom.services.customer.wishlist;

import com.codeWithProjects.ecom.dto.WishlistDto;
import com.codeWithProjects.ecom.entity.Product;
import com.codeWithProjects.ecom.entity.User;
import com.codeWithProjects.ecom.entity.Wishlist;
import com.codeWithProjects.ecom.repository.ProductRepository;
import com.codeWithProjects.ecom.repository.UserRepository;
import com.codeWithProjects.ecom.repository.WishlistRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Cacheable
    public WishlistDto addProductToWishlist(WishlistDto wishlistDto) {
        Optional<Product> optionalProduct = productRepository.findById(wishlistDto.getProductId());
        Optional<User> optionalUser = userRepository.findById(wishlistDto.getUserId());

        if (optionalProduct.isPresent() && optionalUser.isPresent()) {
            // Check if the product is already in the user's wishlist
            Optional<Wishlist> existingWishlistItem = wishlistRepository.findByUserIdAndProductId(wishlistDto.getUserId(), wishlistDto.getProductId());
            if (existingWishlistItem.isPresent()) {
                return existingWishlistItem.get().getWishlistDto(); // Return existing item or handle accordingly
            }

            Wishlist wishlist = new Wishlist();
            wishlist.setProduct(optionalProduct.get());
            wishlist.setUser(optionalUser.get());

            return wishlistRepository.save(wishlist).getWishlistDto();
        }
        return null;
    }

    @Override
    @Cacheable
    public List<WishlistDto> getWishlistByUserId(Long userId) {
        return wishlistRepository.findAllByUserId(userId).stream().map(Wishlist::getWishlistDto).collect(Collectors.toList());
    }

    @Cacheable
    public boolean removeProductFromWishlist(Long userId, Long productId) {
        Optional<Wishlist> wishlistItem = wishlistRepository.findByUserIdAndProductId(userId, productId);
        if (wishlistItem.isPresent()) {
            wishlistRepository.deleteById(wishlistItem.get().getId());
            return true;
        }
        return false;
    }
}
