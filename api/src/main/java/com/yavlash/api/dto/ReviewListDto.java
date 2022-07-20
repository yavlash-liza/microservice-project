package com.yavlash.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReviewListDto {
    private int reviewId;
    private String author;
    private String subject;
    private String content;
}