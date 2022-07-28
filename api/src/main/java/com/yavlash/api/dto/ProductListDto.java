package com.yavlash.api.dto;

import com.yavlash.api.util.ServiceAddresses;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ProductListDto {
    private int productId;
    private String name;
    private int weight;
    private List<RecommendationListDto> recommendations;
    private List<ReviewListDto> reviews;
    private ServiceAddresses serviceAddresses;
}