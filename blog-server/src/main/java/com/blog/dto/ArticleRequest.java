package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ArticleRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200字")
    private String title;

    @Size(max = 500, message = "摘要不能超过500字")
    private String summary;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverImage;
    private Long categoryId;
    private List<Long> tagIds;
    private Integer status;
    private Integer isTop;
    private String series;
    private String scheduledPublishAt;
}
