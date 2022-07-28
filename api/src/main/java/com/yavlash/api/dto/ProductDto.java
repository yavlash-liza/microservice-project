package com.yavlash.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProductDto {
    private int productId;
    private String name;
    private int weight;
    private String serviceAddress;
}