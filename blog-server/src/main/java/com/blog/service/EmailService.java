package com.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendNewCommentNotice(String articleTitle, String authorName, String content) {
        if (mailSender == null) return;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setSubject("[博客] 新评论待审核 - " + articleTitle);
        msg.setText(String.format("""
                你的文章《%s》收到一条新评论：

                评论者：%s
                内容：%s

                请登录后台审核。
                """, articleTitle, authorName, content));
        try { mailSender.send(msg); } catch (Exception ignored) {}
    }

    public void sendReplyNotice(String toEmail, String articleTitle, String replyContent) {
        if (mailSender == null) return;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("[博客] 评论回复通知 - " + articleTitle);
        msg.setText(String.format("""
                你在《%s》的评论收到了回复：

                %s

                前往文章页面查看。
                """, articleTitle, replyContent));
        try { mailSender.send(msg); } catch (Exception ignored) {}
    }

    public void sendVerifyEmail(String toEmail, String token) {
        if (mailSender == null) return;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("[博客] 订阅确认");
        msg.setText(String.format("""
                感谢订阅 DKX's Blog！

                确认链接：http://localhost:5173/api/subscribe/verify?token=%s

                如果这不是你本人的操作，请忽略此邮件。
                """, token));
        try { mailSender.send(msg); } catch (Exception ignored) {}
    }

    public void sendNewArticleNotice(String toEmail, String articleTitle, String articleId) {
        if (mailSender == null) return;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("[博客] 新文章发布 - " + articleTitle);
        msg.setText(String.format("""
                DKX's Blog 发布了新文章：

                《%s》

                立即阅读：http://localhost:5173/article/%s

                如需退订，请回复此邮件。
                """, articleTitle, articleId));
        try { mailSender.send(msg); } catch (Exception ignored) {}
    }
}
