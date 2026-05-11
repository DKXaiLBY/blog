package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.TagVO;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleMapper articleMapper;

    public TagServiceImpl(TagMapper tagMapper, ArticleTagMapper articleTagMapper, ArticleMapper articleMapper) {
        this.tagMapper = tagMapper;
        this.articleTagMapper = articleTagMapper;
        this.articleMapper = articleMapper;
    }

    @Override
    @Cacheable(value = "tags")
    public List<TagVO> listAll() {
        List<Tag> tags = tagMapper.selectList(null);

        var publishedArticleIds = articleMapper.selectList(
                new LambdaQueryWrapper<com.blog.entity.Article>()
                        .eq(com.blog.entity.Article::getStatus, 1))
                .stream().map(com.blog.entity.Article::getId).collect(Collectors.toSet());

        List<ArticleTag> allLinks = articleTagMapper.selectList(null);
        Map<Long, Long> counts = allLinks.stream()
                .filter(at -> publishedArticleIds.contains(at.getArticleId()))
                .collect(Collectors.groupingBy(ArticleTag::getTagId, Collectors.counting()));

        return tags.stream().map(t -> {
            TagVO vo = new TagVO();
            vo.setId(t.getId());
            vo.setName(t.getName());
            vo.setArticleCount(counts.getOrDefault(t.getId(), 0L));
            return vo;
        }).toList();
    }
}
