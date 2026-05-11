package com.blog.controller;

import com.blog.dto.ArticleVO;
import com.blog.dto.PageVO;
import com.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RestController
public class RssController {

    private final ArticleService articleService;

    @Value("${blog.site-url}")
    private String siteUrl;

    public RssController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/api/rss")
    public ResponseEntity<String> rss() {
        PageVO<ArticleVO> page = articleService.listArticles(1, 20, null, null);
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">");
        xml.append("<channel>");
        xml.append("<title>DKX Blog</title>");
        xml.append("<link>").append(siteUrl).append("</link>");
        xml.append("<description>技术分享与生活记录</description>");
        xml.append("<language>zh-CN</language>");
        xml.append("<atom:link href=\"").append(siteUrl).append("/api/rss\" rel=\"self\" type=\"application/rss+xml\"/>");

        DateTimeFormatter rfc822 = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        ZoneId zone = ZoneId.of("Asia/Shanghai");

        for (ArticleVO a : page.getRecords()) {
            ZonedDateTime zdt = a.getCreatedAt().atZone(zone);
            xml.append("<item>");
            xml.append("<title>").append(escapeXml(a.getTitle())).append("</title>");
            xml.append("<link>").append(siteUrl).append("/article/").append(a.getId()).append("</link>");
            xml.append("<guid isPermaLink=\"true\">").append(siteUrl).append("/article/").append(a.getId()).append("</guid>");
            xml.append("<pubDate>").append(rfc822.format(zdt)).append("</pubDate>");
            if (a.getSummary() != null && !a.getSummary().isEmpty()) {
                xml.append("<description><![CDATA[").append(a.getSummary()).append("]]></description>");
            }
            if (a.getCategoryName() != null && !a.getCategoryName().isEmpty()) {
                xml.append("<category>").append(escapeXml(a.getCategoryName())).append("</category>");
            }
            xml.append("</item>");
        }

        xml.append("</channel>");
        xml.append("</rss>");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(xml.toString());
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
