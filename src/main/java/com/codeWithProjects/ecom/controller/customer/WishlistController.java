package com.codeWithProjects.ecom.controller.customer;

import com.codeWithProjects.ecom.dto.WishlistDto;
import com.codeWithProjects.ecom.services.customer.wishlist.WishlistService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
@AllArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/wishlist")
    public ResponseEntity<?> addProductToWishlist(@RequestBody WishlistDto wishlistDto) {
        if (wishlistDto.getUserId() == null || wishlistDto.getProductId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User ID and Product ID must not be null");
        }

        WishlistDto postedWishlistDto = wishlistService.addProductToWishlist(wishlistDto);

        if (postedWishlistDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Product could not be added to the wishlist");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(postedWishlistDto);
    }

    @GetMapping("/wishlist/{userId}")
    public ResponseEntity<?> getWishlistByUserId(@PathVariable Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User ID must not be null");
        }

        List<WishlistDto> wishlist = wishlistService.getWishlistByUserId(userId);

        if (wishlist.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No wishlist found for the given user ID");
        }

        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/wishlist/{userId}/{productId}")
    public ResponseEntity<?> removeProductFromWishlist(@PathVariable Long userId, @PathVariable Long productId) {
        boolean isRemoved = wishlistService.removeProductFromWishlist(userId, productId);

        if (isRemoved) {
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Product removed from wishlist"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Product not found in wishlist"));
        }
    }

}
   