package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.CategoryVO;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.service.CategoryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper, ArticleMapper articleMapper) {
        this.categoryMapper = categoryMapper;
        this.articleMapper = articleMapper;
    }

    @Override
    @Cacheable(value = "categories")
    public List<CategoryVO> listAll() {
        List<Category> categories = categoryMapper.selectList(null);

        var publishedWrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1);
        Map<Long, Long> counts = articleMapper.selectList(publishedWrapper).stream()
                .filter(a -> a != null && a.getCategoryId() != null)
                .collect(Collectors.groupingBy(Article::getCategoryId, Collectors.counting()));

        return categories.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setSortOrder(c.getSortOrder());
            vo.setArticleCount(counts.getOrDefault(c.getId(), 0L));
            return vo;
        }).toList();
    }
}
