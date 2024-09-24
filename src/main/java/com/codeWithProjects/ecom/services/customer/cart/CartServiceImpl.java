package com.codeWithProjects.ecom.services.customer.cart;

import com.codeWithProjects.ecom.dto.AddProductInCartDto;
import com.codeWithProjects.ecom.dto.CartItemsDto;
import com.codeWithProjects.ecom.dto.OrderDto;
import com.codeWithProjects.ecom.dto.PlaceOrderDto;
import com.codeWithProjects.ecom.entity.*;
import com.codeWithProjects.ecom.enums.OrderStatus;
import com.codeWithProjects.ecom.exceptions.ValidationException;
import com.codeWithProjects.ecom.repository.*;
import com.codeWithProjects.ecom.services.customer.razorpay.RazorpayService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private CartItemsRepository cartItemsRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private RazorpayService razorpayService;

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Override
    @Cacheable
    public ResponseEntity<?> addProductToCart(AddProductInCartDto addProductInCartDto) {
        try {
            // Fetch active order or create a new one if none exists
            Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);

            if (activeOrder == null) {
                Optional<User> optionalUser = userRepository.findById(addProductInCartDto.getUserId());
                if (optionalUser.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
                }
                // Create a new order
                activeOrder = new Order();
                activeOrder.setUser(optionalUser.get());
                activeOrder.setOrderStatus(OrderStatus.Pending);
                activeOrder.setTotalAmount(0L);
                activeOrder.setAmount(0L);
                activeOrder.setCartItems(new ArrayList<>());

                // Save the new order
                activeOrder = orderRepository.save(activeOrder);
            }

            Optional<CartItems> optionalCartItems = cartItemsRepository.findByProductIdAndOrderIdAndUserId(
                    addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId());

            if (optionalCartItems.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Product already in cart.");
            } else {
                Optional<Product> optionalProduct = productRepository.findById(addProductInCartDto.getProductId());
                if (optionalProduct.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
                }

                CartItems cartItem = new CartItems();
                cartItem.setProduct(optionalProduct.get());
                cartItem.setPrice(optionalProduct.get().getPrice());
                cartItem.setQuantity(1L);
                cartItem.setUser(activeOrder.getUser());
                cartItem.setOrder(activeOrder);

                CartItems updatedCart = cartItemsRepository.save(cartItem);

                activeOrder.setTotalAmount(activeOrder.getTotalAmount() + cartItem.getPrice());
                activeOrder.setAmount(activeOrder.getAmount() + cartItem.getPrice());
                activeOrder.getCartItems().add(cartItem);

                orderRepository.save(activeOrder);

                return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
            }
        } catch (Exception e) {
            logger.error("Error adding product to cart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while adding the product to the cart");
        }
    }

    @Cacheable
    @Transactional(readOnly = true)
    public OrderDto getCartByUserId(Long userId) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.Pending);
        if (activeOrder == null) {
            throw new ValidationException("No active order found for the user.");
        }
        List<CartItemsDto> cartItemsDtoList = activeOrder.getCartItems().stream().map(CartItems::getCartDto).collect(Collectors.toList());
        OrderDto orderDto = new OrderDto();
        orderDto.setAmount(activeOrder.getAmount());
        orderDto.setId(activeOrder.getId());
        orderDto.setOrderStatus(activeOrder.getOrderStatus());
        orderDto.setDiscount(activeOrder.getDiscount());
        orderDto.setTotalAmount(activeOrder.getTotalAmount());
        orderDto.setCartItems(cartItemsDtoList);
        if (activeOrder.getCoupon() != null) {
            orderDto.setCouponName(activeOrder.getCoupon().getName());
        }

        return orderDto;
    }

    @Cacheable
    public OrderDto applyCoupon(Long userId, String code) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.Pending);
        if (activeOrder == null) {
            throw new ValidationException("No active order found for the user.");
        }
        Coupon coupon = couponRepository.findByCode(code).orElseThrow(() -> new ValidationException("Coupon not found."));

        if (couponIsExpired(coupon)) {
            throw new ValidationException("Coupon has expired.");
        }

        double discountAmount = ((coupon.getDiscount() / 100.0) * activeOrder.getTotalAmount());
        double netAmount = activeOrder.getTotalAmount() - discountAmount;

        activeOrder.setAmount((long) netAmount);
        activeOrder.setDiscount((long) discountAmount);
        activeOrder.setCoupon(coupon);

        orderRepository.save(activeOrder);
        return activeOrder.getOrderDto();
    }

    @Cacheable
    private boolean couponIsExpired(Coupon coupon) {
        Date currentDate = new Date();
        Date expirationDate = coupon.getExpirationDate();

        return expirationDate != null && currentDate.after(expirationDate);
    }

    @Cacheable
    public OrderDto increaseProductQuantity(AddProductInCartDto addProductInCartDto) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);
        if (activeOrder == null) {
            throw new ValidationException("No active order found for the user.");
        }
        Optional<Product> optionalProduct = productRepository.findById(addProductInCartDto.getProductId());

        Optional<CartItems> optionalCartItem = cartItemsRepository.findByProductIdAndOrderIdAndUserId(
                addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId()
        );

        if (optionalProduct.isPresent() && optionalCartItem.isPresent()) {
            CartItems cartItem = optionalCartItem.get();
            Product product = optionalProduct.get();

            activeOrder.setAmount(activeOrder.getAmount() + product.getPrice());
            activeOrder.setTotalAmount(activeOrder.getTotalAmount() + product.getPrice());

            cartItem.setQuantity(cartItem.getQuantity() + 1);

            if (activeOrder.getCoupon() != null) {
                double discountAmount = ((activeOrder.getCoupon().getDiscount() / 100.0) * activeOrder.getTotalAmount());
                double netAmount = activeOrder.getTotalAmount() - discountAmount;

                activeOrder.setAmount((long) netAmount);
                activeOrder.setDiscount((long) discountAmount);
            }

            cartItemsRepository.save(cartItem);
            orderRepository.save(activeOrder);
            return activeOrder.getOrderDto();
        }
        return null;
    }

    @Cacheable
    public OrderDto decreaseProductQuantity(AddProductInCartDto addProductInCartDto) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);
        if (activeOrder == null) {
            throw new ValidationException("No active order found for the user.");
        }
        Optional<Product> optionalProduct = productRepository.findById(addProductInCartDto.getProductId());

        Optional<CartItems> optionalCartItem = cartItemsRepository.findByProductIdAndOrderIdAndUserId(
                addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId()
        );

        if (optionalProduct.isPresent() && optionalCartItem.isPresent()) {
            CartItems cartItem = optionalCartItem.get();
            Product product = optionalProduct.get();

            activeOrder.setAmount(activeOrder.getAmount() - product.getPrice());
            activeOrder.setTotalAmount(activeOrder.getTotalAmount() - product.getPrice());

            cartItem.setQuantity(cartItem.getQuantity() - 1);

            if (activeOrder.getCoupon() != null) {
                double discountAmount = ((activeOrder.getCoupon().getDiscount() / 100.0) * activeOrder.getTotalAmount());
                double netAmount = activeOrder.getTotalAmount() - discountAmount;

                activeOrder.setAmount((long) netAmount);
                activeOrder.setDiscount((long) discountAmount);
            }

            cartItemsRepository.save(cartItem);
            orderRepository.save(activeOrder);
            return activeOrder.getOrderDto();
        }
        return null;
    }

    @Override
    @Cacheable
    public OrderDto placeOrder(PlaceOrderDto placeOrderDto) throws ValidationException{
        // Fetch the active order that is in 'Pending' state
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(placeOrderDto.getUserId(), OrderStatus.Pending);

        // If no active order exists, throw an exception
        if (activeOrder == null) {
            throw new ValidationException("No active order found for the user.");
        }

        // Fetch the user details
        Optional<User> optionalUser = userRepository.findById(placeOrderDto.getUserId());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Set the additional order details
            activeOrder.setOrderDescription(placeOrderDto.getOrderDescription());
            activeOrder.setAddress(placeOrderDto.getAddress());
            activeOrder.setDate(new Date());

            try {
                // Call RazorpayService to create the Razorpay order
                String razorpayOrderId = razorpayService.createOrder(activeOrder.getAmount(), String.valueOf(activeOrder.getId()));

                // Save Razorpay order ID to track the payment in your system
                activeOrder.setRazorpayOrderId(razorpayOrderId);

                // Update order status to 'Placed'
                activeOrder.setOrderStatus(OrderStatus.Placed);
                activeOrder.setTrackingId(UUID.randomUUID());  // Set trackingId here

                // Save the updated order with Razorpay details
                orderRepository.save(activeOrder);

                // Create a new empty order for the user after the current one is placed
                Order newOrder = new Order();
                newOrder.setAmount(0L);
                newOrder.setTotalAmount(0L);
                newOrder.setDiscount(0L);
                newOrder.setUser(user);
                newOrder.setOrderStatus(OrderStatus.Pending);
                orderRepository.save(newOrder);

                // Return the active order details (including Razorpay order ID)
                return activeOrder.getOrderDto();
            } catch (Exception e) {
                throw new ValidationException("Error creating Razorpay order: " + e.getMessage());
            }
        }

        // If user not found, return null
        return null;
    }

    @Cacheable
    public List<OrderDto> getMyPlacedOrders(Long userId) {
        List<OrderStatus> orderStatusList = List.of(OrderStatus.Placed, OrderStatus.Shipped, OrderStatus.Delivered);

        return orderRepository.findAllByUserIdAndOrderStatusIn(userId, orderStatusList).stream().map(Order::getOrderDto).
                collect(Collectors.toList());
    }

    public OrderDto searchOrderByTrackingId(UUID trackingId) {
        Optional<Order> optionalOrder = orderRepository.findByTrackingId(trackingId);

        if (optionalOrder.isPresent()) {
            return optionalOrder.get().getOrderDto();
        }
        return null;
    }

    @Cacheable
    public boolean removeProductFromCart(Long userId, Long productId) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.Pending);
        if (activeOrder == null) {
            throw new ValidationException("No active order found for the user.");
        }

        Optional<CartItems> optionalCartItem = cartItemsRepository.findByProductIdAndOrderIdAndUserId(productId, activeOrder.getId(), userId);

        if (optionalCartItem.isPresent()) {
            CartItems cartItem = optionalCartItem.get();

            // Update the total amount and amount in the active order
            activeOrder.setTotalAmount(activeOrder.getTotalAmount() - (cartItem.getPrice() * cartItem.getQuantity()));
            activeOrder.setAmount(activeOrder.getAmount() - (cartItem.getPrice() * cartItem.getQuantity()));

            // Remove the cart item from the repository
            cartItemsRepository.delete(cartItem);

            // Check if the cart is now empty
            if (activeOrder.getCartItems().isEmpty()) {
                activeOrder.setCoupon(null);
                activeOrder.setDiscount(0L);
            } else if (activeOrder.getCoupon() != null) {
                // Recalculate the discount if the coupon is still valid and the cart is not empty
                double discountAmount = (activeOrder.getCoupon().getDiscount() / 100.0) * activeOrder.getTotalAmount();
                double netAmount = activeOrder.getTotalAmount() - discountAmount;

                activeOrder.setAmount((long) netAmount);
                activeOrder.setDiscount((long) discountAmount);
            }

            // Save the updated active order
            orderRepository.save(activeOrder);
            return true;
        }

        return false; // Return false if the item was not found in the cart
    }


}
