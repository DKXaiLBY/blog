package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ArticleRequest;
import com.blog.dto.ArticleVO;
import com.blog.dto.PageVO;
import com.blog.entity.*;
import com.blog.mapper.*;
import com.blog.common.exception.NotFoundException;
import com.blog.service.ArticleService;
import com.blog.service.EmailService;
import com.blog.service.SubscriberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private static final Logger log = LoggerFactory.getLogger(ArticleServiceImpl.class);

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final SubscriberService subscriberService;

    public ArticleServiceImpl(ArticleMapper articleMapper, CategoryMapper categoryMapper,
                               TagMapper tagMapper, ArticleTagMapper articleTagMapper,
                               UserMapper userMapper, EmailService emailService,
                               SubscriberService subscriberService) {
        this.articleMapper = articleMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.articleTagMapper = articleTagMapper;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.subscriberService = subscriberService;
    }

    @Override
    public PageVO<ArticleVO> listArticles(Integer page, Integer size, Long categoryId, String keyword) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1);
        wrapper.orderByDesc(Article::getIsTop);
        wrapper.orderByDesc(Article::getCreatedAt);

        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Article::getTitle, keyword);
        }

        IPage<Article> iPage = articleMapper.selectPage(new Page<>(page, size), wrapper);
        return buildPageVO(iPage, page, size);
    }

    @Override
    public PageVO<ArticleVO> listAdminArticles(Integer page, Integer size, String keyword, Integer status) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Article::getIsTop);
        wrapper.orderByDesc(Article::getCreatedAt);

        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Article::getTitle, keyword);
        }

        IPage<Article> iPage = articleMapper.selectPage(new Page<>(page, size), wrapper);
        return buildPageVO(iPage, page, size);
    }

    @Override
    public ArticleVO getArticleDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getStatus() != 1) {
            throw new NotFoundException("文章不存在");
        }
        articleMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, id)
                        .setSql("view_count = view_count + 1"));
        article.setViewCount(article.getViewCount() != null ? article.getViewCount() + 1 : 1);
        return toArticleVO(article);
    }

    @Override
    public ArticleVO getArticleDetailForAdmin(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new NotFoundException("文章不存在");
        }
        return toArticleVO(article);
    }

    @Override
    @Transactional
    public void createArticle(ArticleRequest req, Long authorId) {
        Article article = new Article();
        article.setTitle(req.getTitle());
        article.setSummary(req.getSummary());
        article.setContent(req.getContent());
        article.setCoverImage(req.getCoverImage());
        article.setCategoryId(req.getCategoryId());
        article.setAuthorId(authorId);
        article.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        article.setIsTop(req.getIsTop() != null ? req.getIsTop() : 0);
        article.setSeries(req.getSeries());
        if (req.getScheduledPublishAt() != null && !req.getScheduledPublishAt().isBlank()) {
            article.setScheduledPublishAt(java.time.LocalDateTime.parse(req.getScheduledPublishAt()));
        }
        articleMapper.insert(article);

        saveTags(article.getId(), req.getTagIds());

        // Notify subscribers when publishing immediately
        if (req.getStatus() != null && req.getStatus() == 1 && emailService != null) {
            notifySubscribers(article);
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

    @Override
    @Transactional
    public void updateArticle(Long id, ArticleRequest req) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new NotFoundException("文章不存在");
        }
        article.setTitle(req.getTitle());
        article.setSummary(req.getSummary());
        article.setContent(req.getContent());
        article.setCoverImage(req.getCoverImage());
        article.setCategoryId(req.getCategoryId());
        if (req.getStatus() != null) article.setStatus(req.getStatus());
        if (req.getIsTop() != null) article.setIsTop(req.getIsTop());
        article.setSeries(req.getSeries());
        if (req.getScheduledPublishAt() != null && !req.getScheduledPublishAt().isBlank()) {
            article.setScheduledPublishAt(java.time.LocalDateTime.parse(req.getScheduledPublishAt()));
        } else {
            article.setScheduledPublishAt(null);
        }
        articleMapper.updateById(article);

        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, id);
        articleTagMapper.delete(wrapper);
        saveTags(id, req.getTagIds());
    }

    @Override
    public void deleteArticle(Long id) {
        articleMapper.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> listArchives() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1);
        wrapper.orderByDesc(Article::getCreatedAt);
        List<Article> articles = articleMapper.selectList(wrapper);

        Map<String, List<ArticleVO>> grouped = new LinkedHashMap<>();
        for (Article a : articles) {
            String month = a.getCreatedAt().toLocalDate().toString().substring(0, 7);
            grouped.computeIfAbsent(month, k -> new ArrayList<>()).add(toArticleVO(a));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", entry.getKey());
            item.put("articles", entry.getValue());
            item.put("count", entry.getValue().size());
            result.add(item);
        }
        return result;
    }

    @Override
    public void likeArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getStatus() != 1) {
            throw new NotFoundException("文章不存在");
        }
        articleMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, id)
                        .setSql("like_count = COALESCE(like_count, 0) + 1"));
    }

    @Override
    public Map<String, ArticleVO> getPrevNext(Long id) {
        Map<String, ArticleVO> result = new LinkedHashMap<>();

        LambdaQueryWrapper<Article> prevWrapper = new LambdaQueryWrapper<>();
        prevWrapper.eq(Article::getStatus, 1);
        prevWrapper.lt(Article::getId, id);
        prevWrapper.orderByDesc(Article::getId);
        prevWrapper.last("LIMIT 1");
        Article prev = articleMapper.selectOne(prevWrapper);
        result.put("prev", prev != null ? toArticleVO(prev) : null);

        LambdaQueryWrapper<Article> nextWrapper = new LambdaQueryWrapper<>();
        nextWrapper.eq(Article::getStatus, 1);
        nextWrapper.gt(Article::getId, id);
        nextWrapper.orderByAsc(Article::getId);
        nextWrapper.last("LIMIT 1");
        Article next = articleMapper.selectOne(nextWrapper);
        result.put("next", next != null ? toArticleVO(next) : null);

        return result;
    }

    @Override
    public List<ArticleVO> getRelatedArticles(Long id, int limit) {
        Article article = articleMapper.selectById(id);
        if (article == null) return List.of();

        // Get current article's tag IDs
        List<ArticleTag> currentTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        List<Long> tagIds = currentTags.stream().map(ArticleTag::getTagId).toList();

        List<Article> related = new ArrayList<>();
        if (!tagIds.isEmpty()) {
            // Find articles sharing at least one tag
            List<Long> relatedIds = articleTagMapper.selectList(
                    new LambdaQueryWrapper<ArticleTag>()
                            .in(ArticleTag::getTagId, tagIds)
                            .ne(ArticleTag::getArticleId, id)).stream()
                    .map(ArticleTag::getArticleId).distinct().toList();
            if (!relatedIds.isEmpty()) {
                related = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                        .in(Article::getId, relatedIds)
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getCreatedAt)
                        .last("LIMIT " + limit));
            }
        }

        // Fill with same-category articles if not enough
        if (related.size() < limit && article.getCategoryId() != null) {
            List<Long> existingIds = related.stream().map(Article::getId).collect(Collectors.toList());
            existingIds.add(id);
            List<Article> categoryArticles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                    .eq(Article::getCategoryId, article.getCategoryId())
                    .eq(Article::getStatus, 1)
                    .notIn(!existingIds.isEmpty(), Article::getId, existingIds)
                    .orderByDesc(Article::getCreatedAt)
                    .last("LIMIT " + (limit - related.size())));
            related.addAll(categoryArticles);
        }

        // If still not enough, fill with latest articles
        if (related.size() < limit) {
            List<Long> existingIds = related.stream().map(Article::getId).collect(Collectors.toList());
            existingIds.add(id);
            List<Article> latest = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                    .eq(Article::getStatus, 1)
                    .notIn(!existingIds.isEmpty(), Article::getId, existingIds)
                    .orderByDesc(Article::getCreatedAt)
                    .last("LIMIT " + (limit - related.size())));
            related.addAll(latest);
        }

        return related.stream().map(this::toArticleVO).collect(Collectors.toList());
    }

    private void saveTags(Long articleId, List<Long> tagIds) {
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(articleId);
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
    }

    private PageVO<ArticleVO> buildPageVO(IPage<Article> iPage, int page, int size) {
        List<Article> articles = iPage.getRecords();
        if (articles.isEmpty()) {
            return new PageVO<>(List.of(), iPage.getTotal(), page, size);
        }

        var categoryIds = articles.stream().map(Article::getCategoryId)
                .filter(id -> id != null).collect(Collectors.toSet());
        var categories = categoryIds.isEmpty() ? Map.<Long, String>of()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, Category::getName));

        var articleIds = articles.stream().map(Article::getId).toList();
        var allTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIds));
        var tagIdToName = new HashMap<Long, String>();
        if (!allTags.isEmpty()) {
            var tagIds = allTags.stream().map(ArticleTag::getTagId).collect(Collectors.toSet());
            tagMapper.selectBatchIds(tagIds).forEach(t -> tagIdToName.put(t.getId(), t.getName()));
        }
        Map<Long, List<ArticleTag>> tagsByArticle = allTags.stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId));

        var authorIds = articles.stream().map(Article::getAuthorId)
                .filter(id -> id != null).collect(Collectors.toSet());
        var authors = authorIds.isEmpty() ? Map.<Long, String>of()
                : userMapper.selectBatchIds(authorIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getNickname));

        List<ArticleVO> voList = articles.stream().map(a -> {
            ArticleVO vo = new ArticleVO();
            vo.setId(a.getId());
            vo.setTitle(a.getTitle());
            vo.setSummary(a.getSummary());
            vo.setContent(a.getContent());
            vo.setCoverImage(a.getCoverImage());
            vo.setStatus(a.getStatus());
            vo.setViewCount(a.getViewCount());
            vo.setLikeCount(a.getLikeCount());
            vo.setIsTop(a.getIsTop());
            vo.setSeries(a.getSeries());
            vo.setCreatedAt(a.getCreatedAt());
            vo.setUpdatedAt(a.getUpdatedAt());

            vo.setCategoryId(a.getCategoryId());
            if (a.getCategoryId() != null) {
                vo.setCategoryName(categories.get(a.getCategoryId()));
            }

            List<ArticleTag> ats = tagsByArticle.getOrDefault(a.getId(), List.of());
            List<Long> tagIds = new ArrayList<>();
            List<String> tagNames = new ArrayList<>();
            for (ArticleTag at : ats) {
                tagIds.add(at.getTagId());
                String name = tagIdToName.get(at.getTagId());
                if (name != null) tagNames.add(name);
            }
            vo.setTagIds(tagIds);
            vo.setTagNames(tagNames);

            if (a.getAuthorId() != null) {
                vo.setAuthorName(authors.get(a.getAuthorId()));
            }
            return vo;
        }).toList();

        return new PageVO<>(voList, iPage.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            articleMapper.deleteBatchIds(ids);
        }
    }

    @Override
    @Transactional
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids != null && status != null && !ids.isEmpty()) {
            articleMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                            .in(Article::getId, ids)
                            .set(Article::getStatus, status));
        }
    }

    private ArticleVO toArticleVO(Article article) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setContent(article.getContent());
        vo.setCoverImage(article.getCoverImage());
        vo.setStatus(article.getStatus());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setIsTop(article.getIsTop());
        vo.setSeries(article.getSeries());
        vo.setScheduledPublishAt(article.getScheduledPublishAt());
        vo.setCreatedAt(article.getCreatedAt());
        vo.setUpdatedAt(article.getUpdatedAt());

        vo.setCategoryId(article.getCategoryId());
        if (article.getCategoryId() != null) {
            Category cat = categoryMapper.selectById(article.getCategoryId());
            if (cat != null) vo.setCategoryName(cat.getName());
        }

        LambdaQueryWrapper<ArticleTag> atWrapper = new LambdaQueryWrapper<>();
        atWrapper.eq(ArticleTag::getArticleId, article.getId());
        List<ArticleTag> ats = articleTagMapper.selectList(atWrapper);
        List<Long> tagIds = new ArrayList<>();
        List<String> tagNames = new ArrayList<>();
        if (!ats.isEmpty()) {
            java.util.Set<Long> ids = ats.stream().map(ArticleTag::getTagId).collect(Collectors.toSet());
            java.util.Map<Long, String> tagMap = tagMapper.selectBatchIds(ids).stream()
                    .collect(Collectors.toMap(Tag::getId, Tag::getName));
            for (ArticleTag at : ats) {
                tagIds.add(at.getTagId());
                String name = tagMap.get(at.getTagId());
                if (name != null) tagNames.add(name);
            }
        }
        vo.setTagIds(tagIds);
        vo.setTagNames(tagNames);

        if (article.getAuthorId() != null) {
            User author = userMapper.selectById(article.getAuthorId());
            vo.setAuthorName(author != null ? author.getNickname() : null);
        }

        // Series info
        if (article.getSeries() != null && !article.getSeries().isBlank()) {
            List<Article> seriesArticles = articleMapper.selectList(
                    new LambdaQueryWrapper<Article>()
                            .eq(Article::getSeries, article.getSeries())
                            .eq(Article::getStatus, 1)
                            .orderByAsc(Article::getCreatedAt));
            List<ArticleVO> seriesVOs = new ArrayList<>();
            int idx = 0;
            for (int i = 0; i < seriesArticles.size(); i++) {
                Article sa = seriesArticles.get(i);
                ArticleVO svo = new ArticleVO();
                svo.setId(sa.getId());
                svo.setTitle(sa.getTitle());
                svo.setCreatedAt(sa.getCreatedAt());
                seriesVOs.add(svo);
                if (sa.getId().equals(article.getId())) {
                    idx = i + 1;
                }
            }
            vo.setSeriesArticles(seriesVOs);
            vo.setSeriesIndex(idx);
        }

        return vo;
    }
}
