package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.PageVO;
import com.blog.dto.ArticleVO;
import com.blog.service.ArticleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public Result<PageVO<ArticleVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(articleService.listArticles(page, size, categoryId, null));
    }

    @GetMapping("/search")
    public Result<PageVO<ArticleVO>> search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam String keyword) {
        return Result.success(articleService.listArticles(page, size, null, keyword));
    }

    @GetMapping("/archives")
    public Result<List<Map<String, Object>>> archives() {
        return Result.success(articleService.listArchives());
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.getArticleDetail(id));
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        articleService.likeArticle(id);
        return Result.success();
    }

    @GetMapping("/{id}/prev-next")
    public Result<Map<String, ArticleVO>> prevNext(@PathVariable Long id) {
        return Result.success(articleService.getPrevNext(id));
    }

    @GetMapping("/{id}/related")
    public Result<List<ArticleVO>> related(@PathVariable Long id,
                                           @RequestParam(defaultValue = "3") int limit) {
        return Result.success(articleService.getRelatedArticles(id, limit));
    }
}
