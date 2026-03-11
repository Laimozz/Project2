package com.example.Project2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;

    // URL to fetch image from DB
    private String imageUrl;
    private String imageName;
    private String imageType;
    private Boolean hasImage;

    private Integer categoryId;
    private String categoryName;
}
