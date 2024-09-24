package com.codeWithProjects.ecom.services.auth;

import com.codeWithProjects.ecom.dto.SignupRequest;
import com.codeWithProjects.ecom.dto.UserDto;
import com.codeWithProjects.ecom.entity.Order;
import com.codeWithProjects.ecom.entity.User;
import com.codeWithProjects.ecom.enums.OrderStatus;
import com.codeWithProjects.ecom.enums.UserRole;
import com.codeWithProjects.ecom.repository.OrderRepository;
import com.codeWithProjects.ecom.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final OrderRepository orderRepository;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.orderRepository = orderRepository;
    }

    @Override
    @Cacheable
    public UserDto createUser(SignupRequest signupRequest) {
        User user = mapToUser(signupRequest);
        User createdUser = userRepository.save(user);

        createInitialOrderForUser(createdUser);

        return mapToUserDto(createdUser);
    }

    @Override
    @Cacheable
    public Boolean hasUserWithEmail(String email) {
        return userRepository.findFirstByEmail(email).isPresent();
    }

    @PostConstruct
    @Cacheable
    public void createAdminAccount() {
        if (userRepository.findByRole(UserRole.ADMIN) == null) {
            User admin = new User();
            admin.setEmail("admin@gmail.com");
            admin.setName("Admin");
            admin.setRole(UserRole.ADMIN);
            admin.setPassword(bCryptPasswordEncoder.encode("Admin123@#"));
            userRepository.save(admin);
        }
    }

    @Cacheable
    private User mapToUser(SignupRequest signupRequest) {
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setName(signupRequest.getName());
        user.setPassword(bCryptPasswordEncoder.encode(signupRequest.getPassword()));
        user.setRole(UserRole.CUSTOMER);
        return user;
    }

    @Cacheable
    private void createInitialOrderForUser(User user) {
        Order order = new Order();
        order.setAmount(0L);
        order.setTotalAmount(0L);
        order.setDiscount(0L);
        order.setUser(user);
        order.setOrderStatus(OrderStatus.Pending);
        orderRepository.save(order);
    }

    @Cacheable
    private UserDto mapToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());
        userDto.setUserRole(user.getRole());
        return userDto;
    }
}
