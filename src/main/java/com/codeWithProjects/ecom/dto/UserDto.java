package com.codeWithProjects.ecom.dto;

import com.codeWithProjects.ecom.enums.UserRole;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class UserDto {

    private Long id;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Name is mandatory")
    @Pattern(
            regexp = "^[A-Za-z\\s]+$",
            message = "Name should only contain alphabetic characters and spaces"
    )
    private String name;

    @NotBlank(message = "Password is mandatory")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
            message = "Password must be at least 8 characters long, and include one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String password;

    private UserRole userRole;
}
