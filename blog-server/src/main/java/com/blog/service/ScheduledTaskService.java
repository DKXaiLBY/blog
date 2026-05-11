package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.Article;
import com.blog.mapper.ArticleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final ArticleMapper articleMapper;
    private final EmailService emailService;
    private final SubscriberService subscriberService;

    public ScheduledTaskService(ArticleMapper articleMapper, EmailService emailService,
                                SubscriberService subscriberService) {
        this.articleMapper = articleMapper;
        this.emailService = emailService;
        this.subscriberService = subscriberService;
    }

    @Scheduled(fixedRate = 60000)
    public void publishScheduledArticles() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 0);
        wrapper.le(Article::getScheduledPublishAt, LocalDateTime.now());
        var articles = articleMapper.selectList(wrapper);

        for (Article article : articles) {
            article.setStatus(1);
            article.setScheduledPublishAt(null);
            articleMapper.updateById(article);
            log.info("Published scheduled article: '{}' ({})", article.getTitle(), article.getId());

            // Notify subscribers
            if (emailService != null) {
                notifySubscribers(article);
            }
        }
    }

    private void notifySubscribers(Article article) {
        try {
            var subscribers = subscriberService.listAll();
            for (var sub : subscribers) {
                try {
                    emailService.sendNewArticleNotice(sub.getEmail(), article.getTitle(), article.getId().toString());
                } catch (Exception e) {
                    log.warn("Failed to notify subscriber {}: {}", sub.getEmail(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to list subscribers: {}", e.getMessage());
        }
    }
}
