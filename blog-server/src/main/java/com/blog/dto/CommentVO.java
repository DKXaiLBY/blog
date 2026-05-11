package com.blog.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long articleId;
    private String articleTitle;
    private Long parentId;
    private String authorName;
    private String content;
    private Integer status;
    private LocalDateTime createdAt;
}
