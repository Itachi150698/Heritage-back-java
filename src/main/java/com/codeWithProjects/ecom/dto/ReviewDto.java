package com.codeWithProjects.ecom.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ReviewDto {

    private Long id;

    private Long rating;

    private String description;

    private MultipartFile img;

    private byte[] returnedImg;

    private Long userId;

    private Long productId;

    private String username;
}
