package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ArticleRequest;
import com.blog.dto.ArticleVO;
import com.blog.dto.PageVO;
import com.blog.entity.*;
import com.blog.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock ArticleMapper articleMapper;
    @Mock CategoryMapper categoryMapper;
    @Mock TagMapper tagMapper;
    @Mock ArticleTagMapper articleTagMapper;
    @Mock UserMapper userMapper;

    private ArticleServiceImpl articleService;

    private Article article;
    private Category category;
    private Tag tag;
    private User author;

    @BeforeEach
    void setUp() {
        articleService = new ArticleServiceImpl(articleMapper, categoryMapper,
                tagMapper, articleTagMapper, userMapper, null, null);

        category = new Category();
        category.setId(1L);
        category.setName("Tech");

        tag = new Tag();
        tag.setId(1L);
        tag.setName("Java");

        author = new User();
        author.setId(1L);
        author.setNickname("博主");

        article = new Article();
        article.setId(1L);
        article.setTitle("Test Article");
        article.setSummary("Summary");
        article.setContent("# Content\nSome text here.");
        article.setCategoryId(1L);
        article.setAuthorId(1L);
        article.setStatus(1);
        article.setViewCount(10);
        article.setLikeCount(5);
        article.setIsTop(0);
        article.setCreatedAt(LocalDateTime.of(2025, 1, 15, 10, 0));
        article.setUpdatedAt(LocalDateTime.of(2025, 1, 15, 10, 0));
    }

    @SuppressWarnings("unchecked")
    private void mockEmptyPage() {
        IPage<Article> emptyPage = new Page<>(1, 10);
        emptyPage.setRecords(List.of());
        emptyPage.setTotal(0);
        doReturn(emptyPage).when(articleMapper).selectPage(any(), any());
    }

    @Nested
    @DisplayName("listArticles")
    class ListArticlesTests {
        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("should return published articles")
        void shouldReturnPublishedArticles() {
            IPage<Article> iPage = new Page<>(1, 10);
            iPage.setRecords(List.of(article));
            iPage.setTotal(1);
            doReturn(iPage).when(articleMapper).selectPage(any(), any());
            when(categoryMapper.selectBatchIds(anyCollection())).thenReturn(List.of(category));
            when(articleTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
            when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(author));

            PageVO<ArticleVO> result = articleService.listArticles(1, 10, null, null);

            assertEquals(1, result.getTotal());
            assertEquals("Test Article", result.getRecords().get(0).getTitle());
            assertEquals("Tech", result.getRecords().get(0).getCategoryName());
            assertEquals("博主", result.getRecords().get(0).getAuthorName());
        }

        @Test
        @DisplayName("should handle empty result")
        void shouldHandleEmptyResult() {
            mockEmptyPage();
            PageVO<ArticleVO> result = articleService.listArticles(1, 10, null, null);
            assertEquals(0, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("should filter by categoryId")
        void shouldFilterByCategoryId() {
            IPage<Article> iPage = new Page<>(1, 10);
            iPage.setRecords(List.of(article));
            iPage.setTotal(1);
            doReturn(iPage).when(articleMapper).selectPage(any(), any());
            when(categoryMapper.selectBatchIds(anyCollection())).thenReturn(List.of(category));
            when(articleTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
            when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(author));

            PageVO<ArticleVO> result = articleService.listArticles(1, 10, 1L, null);
            assertEquals(1, result.getTotal());
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("should search by keyword")
        void shouldSearchByKeyword() {
            IPage<Article> iPage = new Page<>(1, 10);
            iPage.setRecords(List.of(article));
            iPage.setTotal(1);
            doReturn(iPage).when(articleMapper).selectPage(any(), any());
            when(categoryMapper.selectBatchIds(anyCollection())).thenReturn(List.of(category));
            when(articleTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
            when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(author));

            PageVO<ArticleVO> result = articleService.listArticles(1, 10, null, "Test");
            assertEquals(1, result.getTotal());
        }
    }

    @Nested
    @DisplayName("getArticleDetail")
    class GetArticleDetailTests {
        @Test
        @DisplayName("should return article and increment view count")
        void shouldReturnArticleAndIncrementViews() {
            when(articleMapper.selectById(1L)).thenReturn(article);
            when(articleMapper.updateById(any(Article.class))).thenReturn(1);
            when(categoryMapper.selectById(1L)).thenReturn(category);
            when(articleTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
            when(userMapper.selectById(1L)).thenReturn(author);

            ArticleVO result = articleService.getArticleDetail(1L);

            assertEquals("Test Article", result.getTitle());
            assertEquals(11, article.getViewCount()); // incremented
        }

        @Test
        @DisplayName("should throw when article not found")
        void shouldThrowWhenArticleNotFound() {
            when(articleMapper.selectById(999L)).thenReturn(null);
            assertThrows(RuntimeException.class, () -> articleService.getArticleDetail(999L));
        }

        @Test
        @DisplayName("should throw when article is not published")
        void shouldThrowWhenArticleNotPublished() {
            article.setStatus(0);
            when(articleMapper.selectById(1L)).thenReturn(article);
            assertThrows(RuntimeException.class, () -> articleService.getArticleDetail(1L));
        }
    }

    @Nested
    @DisplayName("createArticle")
    class CreateArticleTests {
        @Test
        @DisplayName("should create article with tags")
        void shouldCreateArticleWithTags() {
            ArticleRequest req = new ArticleRequest();
            req.setTitle("New");
            req.setSummary("Sum");
            req.setContent("Body");
            req.setCategoryId(1L);
            req.setTagIds(List.of(1L, 2L));
            req.setStatus(1);
            req.setIsTop(0);

            when(articleMapper.insert(any(Article.class))).thenAnswer(inv -> {
                Article a = inv.getArgument(0);
                a.setId(10L);
                return 1;
            });
            when(articleTagMapper.insert(any(ArticleTag.class))).thenReturn(1);

            articleService.createArticle(req, 1L);
            verify(articleTagMapper, times(2)).insert(any(ArticleTag.class));
        }
    }

    @Nested
    @DisplayName("updateArticle")
    class UpdateArticleTests {
        @Test
        @DisplayName("should update article and replace tags")
        void shouldUpdateArticleAndReplaceTags() {
            ArticleRequest req = new ArticleRequest();
            req.setTitle("Updated");
            req.setSummary("Sum");
            req.setContent("Body");
            req.setCategoryId(1L);
            req.setTagIds(List.of(3L));
            req.setStatus(1);
            req.setIsTop(0);

            when(articleMapper.selectById(1L)).thenReturn(article);
            when(articleMapper.updateById(any(Article.class))).thenReturn(1);
            when(articleTagMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
            when(articleTagMapper.insert(any(ArticleTag.class))).thenReturn(1);

            articleService.updateArticle(1L, req);

            verify(articleTagMapper).delete(any(LambdaQueryWrapper.class));
            verify(articleTagMapper).insert(any(ArticleTag.class));
            assertEquals("Updated", article.getTitle());
        }

        @Test
        @DisplayName("should throw when article not found")
        void shouldThrowWhenArticleNotFound() {
            when(articleMapper.selectById(999L)).thenReturn(null);
            assertThrows(RuntimeException.class, () -> articleService.updateArticle(999L, new ArticleRequest()));
        }
    }

    @Nested
    @DisplayName("deleteArticle")
    class DeleteArticleTests {
        @Test
        @DisplayName("should delete article by id")
        void shouldDeleteArticleById() {
            when(articleMapper.deleteById(1L)).thenReturn(1);
            articleService.deleteArticle(1L);
            verify(articleMapper).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("listArchives")
    class ListArchivesTests {
        @Test
        @DisplayName("should group articles by month")
        void shouldGroupArticlesByMonth() {
            Article a2 = new Article();
            a2.setId(2L);
            a2.setTitle("Article 2");
            a2.setCategoryId(1L);
            a2.setAuthorId(1L);
            a2.setStatus(1);
            a2.setViewCount(0);
            a2.setLikeCount(0);
            a2.setIsTop(0);
            a2.setCreatedAt(LocalDateTime.of(2025, 2, 10, 10, 0));
            a2.setUpdatedAt(LocalDateTime.of(2025, 2, 10, 10, 0));

            when(articleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(a2, article));
            when(categoryMapper.selectById(anyLong())).thenReturn(category);
            when(articleTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
            when(userMapper.selectById(anyLong())).thenReturn(author);

            List<Map<String, Object>> result = articleService.listArchives();

            assertEquals(2, result.size());
            assertEquals("2025-02", result.get(0).get("month"));
            assertEquals("2025-01", result.get(1).get("month"));
        }
    }

    @Nested
    @DisplayName("likeArticle")
    class LikeArticleTests {
        @Test
        @DisplayName("should increment like count")
        void shouldIncrementLikeCount() {
            when(articleMapper.selectById(1L)).thenReturn(article);
            when(articleMapper.updateById(any(Article.class))).thenReturn(1);

            articleService.likeArticle(1L);
            assertEquals(6, article.getLikeCount());
        }

        @Test
        @DisplayName("should throw when article not found")
        void shouldThrowWhenArticleNotFound() {
            when(articleMapper.selectById(999L)).thenReturn(null);
            assertThrows(RuntimeException.class, () -> articleService.likeArticle(999L));
        }
    }

    @Nested
    @DisplayName("getPrevNext")
    class GetPrevNextTests {
        @Test
        @DisplayName("should return prev and next articles")
        void shouldReturnPrevAndNext() {
            Article prev = new Article();
            prev.setId(2L);
            prev.setTitle("Previous");
            prev.setCategoryId(1L);
            prev.setAuthorId(1L);
            prev.setStatus(1);
            prev.setViewCount(0);
            prev.setLikeCount(0);
            prev.setIsTop(0);
            prev.setCreatedAt(LocalDateTime.now());
            prev.setUpdatedAt(LocalDateTime.now());

            Article next = new Article();
            next.setId(3L);
            next.setTitle("Next");
            next.setCategoryId(1L);
            next.setAuthorId(1L);
            next.setStatus(1);
            next.setViewCount(0);
            next.setLikeCount(0);
            next.setIsTop(0);
            next.setCreatedAt(LocalDateTime.now());
            next.setUpdatedAt(LocalDateTime.now());

            when(articleMapper.selectOne(argThat(w -> w != null))).thenReturn(prev, next);
            when(categoryMapper.selectById(anyLong())).thenReturn(category);
            when(articleTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
            when(userMapper.selectById(anyLong())).thenReturn(author);

            Map<String, ArticleVO> result = articleService.getPrevNext(1L);

            assertNotNull(result.get("prev"));
            assertEquals("Previous", result.get("prev").getTitle());
            assertNotNull(result.get("next"));
            assertEquals("Next", result.get("next").getTitle());
        }
    }

    @Nested
    @DisplayName("getRelatedArticles")
    class GetRelatedArticlesTests {
        @Test
        @DisplayName("should fallback to latest articles when no tags match")
        void shouldFallbackToLatestArticles() {
            when(articleMapper.selectById(1L)).thenReturn(article);
            when(articleTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
                    List.of(),  // current article tags (empty)
                    List.of()   // tag-based search (empty)
            );
            when(categoryMapper.selectById(anyLong())).thenReturn(category);
            when(userMapper.selectById(anyLong())).thenReturn(author);

            Article related = new Article();
            related.setId(5L);
            related.setTitle("Related");
            related.setCategoryId(1L);
            related.setAuthorId(1L);
            related.setStatus(1);
            related.setViewCount(0);
            related.setLikeCount(0);
            related.setIsTop(0);
            related.setCreatedAt(LocalDateTime.now());
            related.setUpdatedAt(LocalDateTime.now());

            when(articleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(related));

            List<ArticleVO> result = articleService.getRelatedArticles(1L, 3);
            assertFalse(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("batch operations")
    class BatchOperationsTests {
        @Test
        @DisplayName("should batch delete articles")
        void shouldBatchDeleteArticles() {
            when(articleMapper.deleteBatchIds(List.of(1L, 2L))).thenReturn(2);
            articleService.batchDelete(List.of(1L, 2L));
            verify(articleMapper).deleteBatchIds(List.of(1L, 2L));
        }

        @Test
        @DisplayName("should handle null ids in batch delete")
        void shouldHandleNullIdsInBatchDelete() {
            articleService.batchDelete(null);
            verify(articleMapper, never()).deleteBatchIds(anyList());
        }

        @Test
        @DisplayName("should batch update status")
        void shouldBatchUpdateStatus() {
            Article a2 = new Article();
            a2.setId(2L);
            a2.setStatus(0);
            when(articleMapper.selectById(1L)).thenReturn(article);
            when(articleMapper.selectById(2L)).thenReturn(a2);
            when(articleMapper.updateById(any(Article.class))).thenReturn(1);

            articleService.batchUpdateStatus(List.of(1L, 2L), 1);
            assertEquals(1, article.getStatus());
            assertEquals(1, a2.getStatus());
        }
    }
}
