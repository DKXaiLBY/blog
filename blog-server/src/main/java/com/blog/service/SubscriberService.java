package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.exception.BadRequestException;
import com.blog.common.exception.NotFoundException;
import com.blog.entity.Article;
import com.blog.entity.Subscriber;
import com.blog.mapper.SubscriberMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SubscriberService {

    private static final Logger log = LoggerFactory.getLogger(SubscriberService.class);

    private final SubscriberMapper subscriberMapper;
    private final EmailService emailService;

    public SubscriberService(SubscriberMapper subscriberMapper, EmailService emailService) {
        this.subscriberMapper = subscriberMapper;
        this.emailService = emailService;
    }

    public void subscribe(String email) {
        LambdaQueryWrapper<Subscriber> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscriber::getEmail, email);
        if (subscriberMapper.selectCount(wrapper) > 0) {
            throw new BadRequestException("该邮箱已订阅");
        }

        Subscriber sub = new Subscriber();
        sub.setEmail(email);
        sub.setVerified(0);
        sub.setVerifyToken(UUID.randomUUID().toString().substring(0, 8));
        subscriberMapper.insert(sub);

        if (emailService != null) {
            try {
                emailService.sendVerifyEmail(email, sub.getVerifyToken());
            } catch (Exception e) {
                log.warn("Failed to send verify email: {}", e.getMessage());
            }
        }
    }

    public void verify(String token) {
        LambdaQueryWrapper<Subscriber> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscriber::getVerifyToken, token);
        Subscriber sub = subscriberMapper.selectOne(wrapper);
        if (sub == null) throw new BadRequestException("无效的验证令牌");
        sub.setVerified(1);
        sub.setVerifyToken(null);
        subscriberMapper.updateById(sub);
    }

    public void unsubscribe(String email) {
        LambdaQueryWrapper<Subscriber> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscriber::getEmail, email);
        subscriberMapper.delete(wrapper);
    }

    public void unsubscribeById(Long id) {
        subscriberMapper.deleteById(id);
    }

    public List<Subscriber> listAll() {
        LambdaQueryWrapper<Subscriber> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscriber::getVerified, 1);
        wrapper.orderByDesc(Subscriber::getCreatedAt);
        return subscriberMapper.selectList(wrapper);
    }

    public long count() {
        LambdaQueryWrapper<Subscriber> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscriber::getVerified, 1);
        return subscriberMapper.selectCount(wrapper);
    }

    public void notifySubscribers(Article article) {
        if (emailService == null) return;
        try {
            for (var sub : listAll()) {
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
