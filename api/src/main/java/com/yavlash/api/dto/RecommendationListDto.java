package com.yavlash.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RecommendationListDto {
    private int recommendationId;
    private String author;
    private int rate;
    private String content;
}