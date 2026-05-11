package com.blog.service;

import com.blog.dto.ArticleRequest;
import com.blog.dto.ArticleVO;
import com.blog.dto.PageVO;

import java.util.List;
import java.util.Map;

public interface ArticleService {
    PageVO<ArticleVO> listArticles(Integer page, Integer size, Long categoryId, String keyword);
    ArticleVO getArticleDetail(Long id);
    void createArticle(ArticleRequest request, Long authorId);
    void updateArticle(Long id, ArticleRequest request);
    void deleteArticle(Long id);
    PageVO<ArticleVO> listAdminArticles(Integer page, Integer size, String keyword, Integer status);
    ArticleVO getArticleDetailForAdmin(Long id);
    List<Map<String, Object>> listArchives();
    void likeArticle(Long id);
    Map<String, ArticleVO> getPrevNext(Long id);
    List<ArticleVO> getRelatedArticles(Long id, int limit);
    void batchDelete(List<Long> ids);
    void batchUpdateStatus(List<Long> ids, Integer status);
}
