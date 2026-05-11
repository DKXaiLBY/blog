package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.JwtUtils;
import com.blog.dto.CommentRequest;
import com.blog.dto.CommentVO;
import com.blog.dto.PageVO;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.service.CommentService;
import com.blog.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    public CommentServiceImpl(CommentMapper commentMapper, ArticleMapper articleMapper,
                              JwtUtils jwtUtils, EmailService emailService) {
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
    }

    @Override
    public List<Comment> getCommentsByArticleId(Long articleId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId);
        wrapper.eq(Comment::getStatus, 1); // only approved
        wrapper.orderByAsc(Comment::getCreatedAt);
        List<Comment> allComments = commentMapper.selectList(wrapper);

        Map<Long, List<Comment>> repliesMap = allComments.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentId));

        List<Comment> topLevel = new ArrayList<>();
        for (Comment c : allComments) {
            if (c.getParentId() == null) {
                c.setReplies(repliesMap.getOrDefault(c.getId(), List.of()));
                topLevel.add(c);
            }
        }
        return topLevel;
    }

    @Override
    public void addComment(CommentRequest req) {
        if (req.getAuthorName() == null || req.getAuthorName().isBlank()) {
            throw new RuntimeException("昵称不能为空");
        }
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new RuntimeException("评论内容不能为空");
        }
        // Validate captcha before allowing comment (after name/content checks for better UX)
        if (req.getCaptchaKey() == null || req.getCaptchaAnswer() == null) {
            throw new RuntimeException("请完成验证码");
        }
        try {
            int expected = jwtUtils.validateCaptchaToken(req.getCaptchaKey());
            if (expected != req.getCaptchaAnswer()) {
                throw new RuntimeException("验证码错误");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("验证码已过期，请刷新重试");
        }
        Comment comment = new Comment();
        comment.setArticleId(req.getArticleId());
        comment.setParentId(req.getParentId());
        comment.setAuthorName(req.getAuthorName());
        comment.setEmail(req.getEmail());
        comment.setContent(req.getContent());
        comment.setStatus(0); // pending approval
        commentMapper.insert(comment);

        // Send email notifications
        if (emailService != null) {
            Article article = articleMapper.selectById(req.getArticleId());
            String title = article != null ? article.getTitle() : "文章";
            try {
                emailService.sendNewCommentNotice(title, req.getAuthorName(), req.getContent());
            } catch (Exception e) {
                log.warn("Failed to send admin notice: {}", e.getMessage());
            }

            // If reply, notify the parent comment author
            if (req.getParentId() != null) {
                try {
                    Comment parent = commentMapper.selectById(req.getParentId());
                    if (parent != null && parent.getEmail() != null && !parent.getEmail().isBlank()) {
                        emailService.sendReplyNotice(parent.getEmail(), title, req.getContent());
                    }
                } catch (Exception e) {
                    log.warn("Failed to send reply notice: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public void deleteComment(Long id) {
        commentMapper.deleteById(id);
    }

    @Override
    public PageVO<CommentVO> listAdminComments(Integer page, Integer size, Long articleId, Integer status) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }
        if (status != null) {
            wrapper.eq(Comment::getStatus, status);
        }
        wrapper.orderByDesc(Comment::getCreatedAt);

        IPage<Comment> iPage = commentMapper.selectPage(new Page<>(page, size), wrapper);

        var articleIds = iPage.getRecords().stream()
                .map(Comment::getArticleId).collect(Collectors.toSet());
        Map<Long, String> titles = articleIds.isEmpty() ? Map.of() :
                articleMapper.selectBatchIds(articleIds).stream()
                        .collect(Collectors.toMap(Article::getId, Article::getTitle));

        List<CommentVO> vos = iPage.getRecords().stream().map(c -> {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setArticleId(c.getArticleId());
            vo.setArticleTitle(titles.get(c.getArticleId()));
            vo.setParentId(c.getParentId());
            vo.setAuthorName(c.getAuthorName());
            vo.setContent(c.getContent());
            vo.setStatus(c.getStatus());
            vo.setCreatedAt(c.getCreatedAt());
            return vo;
        }).toList();

        return new PageVO<>(vos, iPage.getTotal(), page, size);
    }

    @Override
    public void updateCommentStatus(Long id, Integer status) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) throw new RuntimeException("评论不存在");
        comment.setStatus(status);
        commentMapper.updateById(comment);

        // If approved and commenter provided email, notify them
        if (emailService != null && status == 1 && comment.getEmail() != null && !comment.getEmail().isBlank()) {
            try {
                Article article = articleMapper.selectById(comment.getArticleId());
                String title = article != null ? article.getTitle() : "文章";
                emailService.sendReplyNotice(comment.getEmail(), title,
                    "你的评论已通过审核并展示。");
            } catch (Exception e) {
                log.warn("Failed to send approval email: {}", e.getMessage());
            }
        }
    }
}
