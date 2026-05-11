package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.JwtUtils;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.TagMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    public UserServiceImpl(UserMapper userMapper, JwtUtils jwtUtils,
                           ArticleMapper articleMapper, CommentMapper commentMapper,
                           CategoryMapper categoryMapper, TagMapper tagMapper) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
        this.articleMapper = articleMapper;
        this.commentMapper = commentMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
    }

    @Override
    public String login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        return jwtUtils.generateToken(user.getId(), user.getUsername());
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> listAll() {
        return userMapper.selectList(null);
    }

    @Override
    public void updateAvatar(Long id, String avatar) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setAvatar(avatar);
        userMapper.updateById(user);
    }

    @Override
    public void updateProfile(Long id, String nickname, String tagline, String bio, String skills, String contacts, String projects, String announcement) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        if (nickname != null) user.setNickname(nickname);
        if (tagline != null) user.setTagline(tagline);
        if (bio != null) user.setBio(bio);
        if (skills != null) user.setSkills(skills);
        if (contacts != null) user.setContacts(contacts);
        if (projects != null) user.setProjects(projects);
        if (announcement != null) user.setAnnouncement(announcement);
        userMapper.updateById(user);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userMapper.updateById(user);
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalArticles = articleMapper.selectCount(null);
        LambdaQueryWrapper<Article> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.eq(Article::getStatus, 1);
        long publishedArticles = articleMapper.selectCount(publishedWrapper);

        long totalComments = commentMapper.selectCount(null);
        long totalCategories = categoryMapper.selectCount(null);
        long totalTags = tagMapper.selectCount(null);

        List<Article> allArticles = articleMapper.selectList(null);
        long totalViews = allArticles.stream().mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0).sum();
        long totalLikes = allArticles.stream().mapToLong(a -> a.getLikeCount() != null ? a.getLikeCount() : 0).sum();

        stats.put("totalArticles", totalArticles);
        stats.put("publishedArticles", publishedArticles);
        stats.put("draftArticles", totalArticles - publishedArticles);
        stats.put("totalComments", totalComments);
        stats.put("totalCategories", totalCategories);
        stats.put("totalTags", totalTags);
        stats.put("totalViews", totalViews);
        stats.put("totalLikes", totalLikes);

        long totalWords = allArticles.stream()
                .mapToLong(a -> a.getContent() != null ? a.getContent().replaceAll("[#*\\-\\s`>\\[\\]()!|~\\n]", "").length() : 0)
                .sum();
        stats.put("totalWords", totalWords);

        // Monthly article trends (last 6 months)
        Map<String, Long> monthlyTrend = new LinkedHashMap<>();
        java.time.LocalDate now = java.time.LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate month = now.minusMonths(i);
            String key = month.toString().substring(0, 7);
            monthlyTrend.put(key, 0L);
        }
        for (Article a : allArticles) {
            if (a.getCreatedAt() != null) {
                String key = a.getCreatedAt().toLocalDate().toString().substring(0, 7);
                monthlyTrend.computeIfPresent(key, (k, v) -> v + 1);
            }
        }
        stats.put("monthlyTrend", monthlyTrend);

        // Top 5 articles by view count
        List<Map<String, Object>> topArticles = allArticles.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 1)
                .sorted((a, b) -> Long.compare(
                        b.getViewCount() != null ? b.getViewCount() : 0,
                        a.getViewCount() != null ? a.getViewCount() : 0))
                .limit(5)
                .map(a -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", a.getId());
                    item.put("title", a.getTitle());
                    item.put("viewCount", a.getViewCount() != null ? a.getViewCount() : 0);
                    return item;
                }).toList();
        stats.put("topArticles", topArticles);

        // Average views per published article
        long avgViews = publishedArticles > 0 ? totalViews / publishedArticles : 0;
        stats.put("avgViews", avgViews);

        // Most popular category (by article count)
        List<com.blog.entity.Category> allCategories = categoryMapper.selectList(null);
        String topCategory = allCategories.stream()
                .max((a, b) -> {
                    long ca = allArticles.stream().filter(ar -> a.getId().equals(ar.getCategoryId())).count();
                    long cb = allArticles.stream().filter(ar -> b.getId().equals(ar.getCategoryId())).count();
                    return Long.compare(ca, cb);
                })
                .map(com.blog.entity.Category::getName)
                .orElse("-");
        stats.put("topCategory", topCategory);

        // This week's new articles
        java.time.LocalDateTime weekAgo = java.time.LocalDateTime.now().minusDays(7);
        long thisWeekArticles = allArticles.stream()
                .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isAfter(weekAgo))
                .count();
        stats.put("thisWeekArticles", thisWeekArticles);

        return stats;
    }
}
