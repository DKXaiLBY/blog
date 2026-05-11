package com.blog.controller;

import com.blog.dto.ArticleVO;
import com.blog.dto.PageVO;
import com.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

@RestController
public class SitemapController {

    private final ArticleService articleService;

    @Value("${blog.site-url}")
    private String siteUrl;

    public SitemapController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping(value = "/api/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        PageVO<ArticleVO> page = articleService.listArticles(1, 1000, null, null);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        sb.append("  <url>\n");
        sb.append("    <loc>").append(siteUrl).append("</loc>\n");
        sb.append("    <changefreq>daily</changefreq>\n");
        sb.append("    <priority>1.0</priority>\n");
        sb.append("  </url>\n");

        for (ArticleVO a : page.getRecords()) {
            sb.append("  <url>\n");
            sb.append("    <loc>").append(siteUrl).append("/article/").append(a.getId()).append("</loc>\n");
            if (a.getUpdatedAt() != null) {
                sb.append("    <lastmod>").append(a.getUpdatedAt().format(fmt)).append("</lastmod>\n");
            } else if (a.getCreatedAt() != null) {
                sb.append("    <lastmod>").append(a.getCreatedAt().format(fmt)).append("</lastmod>\n");
            }
            sb.append("    <changefreq>monthly</changefreq>\n");
            sb.append("    <priority>0.8</priority>\n");
            sb.append("  </url>\n");
        }

        sb.append("  <url>\n");
        sb.append("    <loc>").append(siteUrl).append("/about</loc>\n");
        sb.append("    <changefreq>monthly</changefreq>\n");
        sb.append("    <priority>0.6</priority>\n");
        sb.append("  </url>\n");

        sb.append("  <url>\n");
        sb.append("    <loc>").append(siteUrl).append("/archive</loc>\n");
        sb.append("    <changefreq>weekly</changefreq>\n");
        sb.append("    <priority>0.7</priority>\n");
        sb.append("  </url>\n");

        sb.append("  <url>\n");
        sb.append("    <loc>").append(siteUrl).append("/tags</loc>\n");
        sb.append("    <changefreq>weekly</changefreq>\n");
        sb.append("    <priority>0.5</priority>\n");
        sb.append("  </url>\n");

        sb.append("  <url>\n");
        sb.append("    <loc>").append(siteUrl).append("/friends</loc>\n");
        sb.append("    <changefreq>monthly</changefreq>\n");
        sb.append("    <priority>0.4</priority>\n");
        sb.append("  </url>\n");

        sb.append("</urlset>\n");

        return ResponseEntity.ok(sb.toString());
    }
}
