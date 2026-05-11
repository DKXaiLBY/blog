package com.blog.dto;

import lombok.Data;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private Integer sortOrder;
    private Long articleCount;
}
