package com.codeWithProjects.ecom.controller.customer;

import com.codeWithProjects.ecom.dto.AddProductInCartDto;
import com.codeWithProjects.ecom.dto.OrderDto;
import com.codeWithProjects.ecom.dto.PlaceOrderDto;
import com.codeWithProjects.ecom.exceptions.ValidationException;
import com.codeWithProjects.ecom.services.customer.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/cart")
    public ResponseEntity<?> addProductToCart(@RequestBody AddProductInCartDto addProductInCartDto){
        return cartService.addProductToCart(addProductInCartDto);
    }

    @GetMapping("/cart/{userId}")
    public ResponseEntity<?> getCartByUserId(@PathVariable Long userId){
        OrderDto orderDto = cartService.getCartByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(orderDto);
    }

    @GetMapping("/coupon/{userId}/{code}")
    public ResponseEntity<?> applyCoupon(@PathVariable Long userId, @PathVariable String code){
        try {
            OrderDto orderDto = cartService.applyCoupon(userId, code);
            return ResponseEntity.ok(orderDto);
        }catch (ValidationException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/addition")
    public ResponseEntity<OrderDto> increaseProductQuantity(@RequestBody AddProductInCartDto addProductInCartDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.increaseProductQuantity(addProductInCartDto));
    }

    @PostMapping("/deduction")
    public ResponseEntity<OrderDto> decreaseProductQuantity(@RequestBody AddProductInCartDto addProductInCartDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.decreaseProductQuantity(addProductInCartDto));
    }

    @PostMapping("/placeOrder")
    public ResponseEntity<?> placeOrder(@RequestBody PlaceOrderDto placeOrderDto) {
        try {
            // Call CartService to place the order
            OrderDto orderDto = cartService.placeOrder(placeOrderDto);

            // Optionally, you can retrieve Razorpay order details here if needed
            // For example, if you want to return the Razorpay order ID along with the order DTO
            String razorpayOrderId = orderDto.getRazorpayOrderId(); // Make sure this is set in `OrderDto`

            // Return the created OrderDto in case of success
            return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
        } catch (ValidationException ex) {
            // Return a different ResponseEntity type in case of validation exception
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", ex.getMessage()) // Wrap error message in a Map or create a custom error response object
            );
        } catch (Exception e) {
            // Return a different ResponseEntity type in case of any other exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "An error occurred while placing the order.")
            );
        }
    }


    @GetMapping("/myOrders/{userId}")
    public ResponseEntity<List<OrderDto>> getMyPlacedOrders(@PathVariable Long userId){
        return ResponseEntity.ok(cartService.getMyPlacedOrders(userId));
    }

    @DeleteMapping("/cart/{userId}/{productId}")
    public ResponseEntity<?> removeProductFromCart(@PathVariable Long userId, @PathVariable Long productId) {
        boolean removed = cartService.removeProductFromCart(userId, productId);
        if (removed) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in cart.");
        }
    }

}
