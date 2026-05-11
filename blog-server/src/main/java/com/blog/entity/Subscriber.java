package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("blog_subscriber")
public class Subscriber {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private Integer verified; // 0=unverified, 1=verified
    private String verifyToken;
    private LocalDateTime createdAt;
}
