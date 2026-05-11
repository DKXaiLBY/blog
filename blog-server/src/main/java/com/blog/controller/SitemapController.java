package com.blog.controller;

import com.blog.entity.Article;
import com.blog.mapper.ArticleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class SitemapController {

    private final ArticleMapper articleMapper;

    public SitemapController(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @GetMapping(value = "/api/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getCreatedAt));

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Homepage
        sb.append("  <url>\n");
        sb.append("    <loc>https://blog.dkx.com/blog</loc>\n");
        sb.append("    <changefreq>daily</changefreq>\n");
        sb.append("    <priority>1.0</priority>\n");
        sb.append("  </url>\n");

        // Articles
        for (Article a : articles) {
            sb.append("  <url>\n");
            sb.append("    <loc>https://blog.dkx.com/article/").append(a.getId()).append("</loc>\n");
            if (a.getUpdatedAt() != null) {
                sb.append("    <lastmod>").append(a.getUpdatedAt().format(fmt)).append("</lastmod>\n");
            } else if (a.getCreatedAt() != null) {
                sb.append("    <lastmod>").append(a.getCreatedAt().format(fmt)).append("</lastmod>\n");
            }
            sb.append("    <changefreq>monthly</changefreq>\n");
            sb.append("    <priority>0.8</priority>\n");
            sb.append("  </url>\n");
        }

        // Static pages
        sb.append("  <url>\n");
        sb.append("    <loc>https://blog.dkx.com/about</loc>\n");
        sb.append("    <changefreq>monthly</changefreq>\n");
        sb.append("    <priority>0.6</priority>\n");
        sb.append("  </url>\n");

        sb.append("  <url>\n");
        sb.append("    <loc>https://blog.dkx.com/archive</loc>\n");
        sb.append("    <changefreq>weekly</changefreq>\n");
        sb.append("    <priority>0.7</priority>\n");
        sb.append("  </url>\n");

        sb.append("  <url>\n");
        sb.append("    <loc>https://blog.dkx.com/tags</loc>\n");
        sb.append("    <changefreq>weekly</changefreq>\n");
        sb.append("    <priority>0.5</priority>\n");
        sb.append("  </url>\n");

        sb.append("  <url>\n");
        sb.append("    <loc>https://blog.dkx.com/friends</loc>\n");
        sb.append("    <changefreq>monthly</changefreq>\n");
        sb.append("    <priority>0.4</priority>\n");
        sb.append("  </url>\n");

        sb.append("</urlset>\n");

        return ResponseEntity.ok(sb.toString());
    }
}
