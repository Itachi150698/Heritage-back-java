package com.codeWithProjects.ecom.entity;

import com.codeWithProjects.ecom.dto.ProductDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Base64;

@Entity
@Data
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long price;

    @Lob
    private String description;

    @Lob
    @Column(columnDefinition = "longblob")
    private byte[] img;

    private Boolean availability;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Category category;

    public ProductDto getDto() {
        ProductDto productDto = new ProductDto();
        productDto.setId(this.id);
        productDto.setName(this.name);
        productDto.setPrice(this.price);
        productDto.setDescription(this.description);
        productDto.setByteImg(this.img);
        productDto.setCategoryId(this.category.getId());
        productDto.setCategoryName(this.category.getName());
        productDto.setAvailability(this.availability);
        return productDto;
    }
}
