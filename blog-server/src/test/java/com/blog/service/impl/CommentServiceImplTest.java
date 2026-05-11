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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommentServiceImplTest {

    @Mock CommentMapper commentMapper;
    @Mock ArticleMapper articleMapper;

    private JwtUtils jwtUtils;
    private CommentServiceImpl commentService;

    private Comment comment1;
    private Comment comment2;
    private Comment reply;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils("test-secret-key-for-jwt-testing-must-be-long-enough", 3600000);
        commentService = new CommentServiceImpl(commentMapper, articleMapper, jwtUtils, null);
        comment1 = new Comment();
        comment1.setId(1L);
        comment1.setArticleId(1L);
        comment1.setAuthorName("User1");
        comment1.setContent("Great article!");
        comment1.setCreatedAt(LocalDateTime.of(2025, 1, 15, 10, 0));

        comment2 = new Comment();
        comment2.setId(2L);
        comment2.setArticleId(1L);
        comment2.setAuthorName("User2");
        comment2.setContent("Nice!");
        comment2.setCreatedAt(LocalDateTime.of(2025, 1, 15, 11, 0));

        reply = new Comment();
        reply.setId(3L);
        reply.setArticleId(1L);
        reply.setParentId(1L);
        reply.setAuthorName("博主");
        reply.setContent("Thanks!");
        reply.setCreatedAt(LocalDateTime.of(2025, 1, 15, 12, 0));
    }

    @Nested
    @DisplayName("getCommentsByArticleId")
    class GetCommentsByArticleIdTests {
        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("should return nested comments with replies")
        void shouldReturnNestedComments() {
            when(commentMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(comment1, comment2, reply));

            List<Comment> result = commentService.getCommentsByArticleId(1L);

            assertEquals(2, result.size()); // 2 top-level
            Comment first = result.get(0);
            assertEquals("Great article!", first.getContent());
            assertEquals(1, first.getReplies().size()); // has 1 reply
            assertEquals("Thanks!", first.getReplies().get(0).getContent());
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("should return empty list when no comments")
        void shouldReturnEmptyWhenNoComments() {
            when(commentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
            List<Comment> result = commentService.getCommentsByArticleId(1L);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("addComment")
    class AddCommentTests {
        @Test
        @DisplayName("should add top-level comment")
        void shouldAddTopLevelComment() {
            String key = jwtUtils.generateCaptchaToken(7);
            CommentRequest req = new CommentRequest();
            req.setArticleId(1L);
            req.setAuthorName("NewUser");
            req.setContent("Hello!");
            req.setCaptchaKey(key);
            req.setCaptchaAnswer(7);

            Article article = new Article();
            article.setId(1L);
            article.setTitle("Test Article");
            when(articleMapper.selectById(1L)).thenReturn(article);
            when(commentMapper.insert(any(Comment.class))).thenReturn(1);

            commentService.addComment(req);
            verify(commentMapper).insert(any(Comment.class));
        }

        @Test
        @DisplayName("should add reply to comment")
        void shouldAddReply() {
            String key = jwtUtils.generateCaptchaToken(5);
            CommentRequest req = new CommentRequest();
            req.setArticleId(1L);
            req.setParentId(1L);
            req.setAuthorName("Replier");
            req.setContent("Reply!");
            req.setCaptchaKey(key);
            req.setCaptchaAnswer(5);

            Article article = new Article();
            article.setId(1L);
            article.setTitle("Test Article");
            when(articleMapper.selectById(1L)).thenReturn(article);
            when(commentMapper.insert(any(Comment.class))).thenReturn(1);

            commentService.addComment(req);
            verify(commentMapper).insert(any(Comment.class));
        }

        @Test
        @DisplayName("should throw when author name is empty")
        void shouldThrowWhenAuthorNameEmpty() {
            CommentRequest req = new CommentRequest();
            req.setAuthorName("");
            req.setContent("Hello");

            assertThrows(RuntimeException.class, () -> commentService.addComment(req));
            verify(commentMapper, never()).insert(any());
        }

        @Test
        @DisplayName("should throw when content is empty")
        void shouldThrowWhenContentEmpty() {
            CommentRequest req = new CommentRequest();
            req.setAuthorName("User");
            req.setContent("  ");

            assertThrows(RuntimeException.class, () -> commentService.addComment(req));
            verify(commentMapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("deleteComment")
    class DeleteCommentTests {
        @Test
        @DisplayName("should delete comment by id")
        void shouldDeleteComment() {
            when(commentMapper.deleteById(1L)).thenReturn(1);
            commentService.deleteComment(1L);
            verify(commentMapper).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("listAdminComments")
    class ListAdminCommentsTests {
        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("should return paginated comments with article titles")
        void shouldReturnPaginatedComments() {
            IPage<Comment> iPage = new Page<>(1, 10);
            iPage.setRecords(List.of(comment1, reply));
            iPage.setTotal(2);
            doReturn(iPage).when(commentMapper).selectPage(any(), any());

            Article article = new Article();
            article.setId(1L);
            article.setTitle("Test Article");
            when(articleMapper.selectBatchIds(anyCollection())).thenReturn(List.of(article));

            PageVO<CommentVO> result = commentService.listAdminComments(1, 10, null, null);

            assertEquals(2, result.getTotal());
            assertEquals("Test Article", result.getRecords().get(0).getArticleTitle());
            assertEquals("Great article!", result.getRecords().get(0).getContent());
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("should filter by articleId")
        void shouldFilterByArticleId() {
            IPage<Comment> iPage = new Page<>(1, 10);
            iPage.setRecords(List.of(comment1));
            iPage.setTotal(1);
            doReturn(iPage).when(commentMapper).selectPage(any(), any());

            Article article = new Article();
            article.setId(1L);
            article.setTitle("Test Article");
            when(articleMapper.selectBatchIds(anyCollection())).thenReturn(List.of(article));

            PageVO<CommentVO> result = commentService.listAdminComments(1, 10, 1L, null);
            assertEquals(1, result.getTotal());
        }
    }

    @Nested
    @DisplayName("updateCommentStatus")
    class UpdateCommentStatusTests {
        @Test
        @DisplayName("should approve a pending comment")
        void shouldApproveComment() {
            comment1.setStatus(0);
            when(commentMapper.selectById(1L)).thenReturn(comment1);
            when(commentMapper.updateById(any(Comment.class))).thenReturn(1);

            commentService.updateCommentStatus(1L, 1);
            assertEquals(1, comment1.getStatus());
        }

        @Test
        @DisplayName("should reject a comment")
        void shouldRejectComment() {
            comment1.setStatus(0);
            when(commentMapper.selectById(1L)).thenReturn(comment1);
            when(commentMapper.updateById(any(Comment.class))).thenReturn(1);

            commentService.updateCommentStatus(1L, 2);
            assertEquals(2, comment1.getStatus());
        }

        @Test
        @DisplayName("should throw when comment not found")
        void shouldThrowWhenCommentNotFound() {
            when(commentMapper.selectById(999L)).thenReturn(null);
            assertThrows(RuntimeException.class,
                () -> commentService.updateCommentStatus(999L, 1));
        }
    }
}
