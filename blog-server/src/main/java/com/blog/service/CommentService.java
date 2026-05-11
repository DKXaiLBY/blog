package com.blog.service;

import com.blog.dto.CommentRequest;
import com.blog.dto.CommentVO;
import com.blog.dto.PageVO;
import com.blog.entity.Comment;
import java.util.List;

public interface CommentService {
    List<Comment> getCommentsByArticleId(Long articleId);
    void addComment(CommentRequest request);
    void deleteComment(Long id);
    PageVO<CommentVO> listAdminComments(Integer page, Integer size, Long articleId, Integer status);
    void updateCommentStatus(Long id, Integer status);
}
