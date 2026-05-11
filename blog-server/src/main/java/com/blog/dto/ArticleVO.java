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
    private Long categoryId;
    private String categoryName;
    private List<Long> tagIds;
    private List<String> tagNames;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer isTop;
    private String series;
    private String authorName;
    // Series info (computed)
    private List<ArticleVO> seriesArticles;
    private Integer seriesIndex;
    private Integer wordCount;
    private Integer readingTime;
    private LocalDateTime scheduledPublishAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
