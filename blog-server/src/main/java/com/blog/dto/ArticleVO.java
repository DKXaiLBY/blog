package com.blog.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleVO {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private String categoryName;
    private List<String> tagNames;
    private Integer viewCount;
    private Integer isTop;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
