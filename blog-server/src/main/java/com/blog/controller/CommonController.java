package com.blog.controller;

import com.blog.common.Result;
import com.blog.common.exception.BadRequestException;
import com.blog.dto.CategoryVO;
import com.blog.dto.CommentRequest;
import com.blog.dto.TagVO;
import com.blog.dto.ArticleVO;
import com.blog.dto.PageVO;
import com.blog.entity.Comment;
import com.blog.entity.FriendLink;
import com.blog.entity.User;
import com.blog.service.ArticleService;
import com.blog.service.CategoryService;
import com.blog.service.CommentService;
import com.blog.service.FriendLinkService;
import com.blog.service.SubscriberService;
import com.blog.service.TagService;
import com.blog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommonController {

    private final CommentService commentService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final UserService userService;
    private final ArticleService articleService;
    private final FriendLinkService friendLinkService;
    private final SubscriberService subscriberService;

    public CommonController(CommentService commentService,
                            CategoryService categoryService,
                            TagService tagService,
                            UserService userService,
                            ArticleService articleService,
                            FriendLinkService friendLinkService,
                            SubscriberService subscriberService) {
        this.commentService = commentService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.userService = userService;
        this.articleService = articleService;
        this.friendLinkService = friendLinkService;
        this.subscriberService = subscriberService;
    }

    @GetMapping("/articles/{id}/comments")
    public Result<List<Comment>> getComments(@PathVariable Long id) {
        return Result.success(commentService.getCommentsByArticleId(id));
    }

    @PostMapping("/comments")
    public Result<Void> addComment(@Valid @RequestBody CommentRequest request) {
        commentService.addComment(request);
        return Result.success();
    }

    @GetMapping("/categories")
    public Result<List<CategoryVO>> getCategories() {
        return Result.success(categoryService.listAll());
    }

    @GetMapping("/tags")
    public Result<List<TagVO>> getTags() {
        return Result.success(tagService.listAll());
    }

    @GetMapping("/user/profile")
    public Result<Map<String, Object>> getProfile() {
        List<User> users = userService.listAll();
        if (users.isEmpty()) {
            return Result.success(Map.of("nickname", "管理员", "avatar", ""));
        }
        User user = users.get(0);
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("nickname", user.getNickname());
        profile.put("avatar", user.getAvatar());
        profile.put("tagline", user.getTagline());
        profile.put("bio", user.getBio());
        profile.put("skills", user.getSkills());
        profile.put("contacts", user.getContacts());
        profile.put("projects", user.getProjects());
        profile.put("announcement", user.getAnnouncement());
        profile.put("createdAt", user.getCreatedAt());
        return Result.success(profile);
    }

    @GetMapping("/featured")
    public Result<ArticleVO> getFeaturedArticle() {
        PageVO<ArticleVO> page = articleService.listArticles(1, 1, null, null);
        if (page.getRecords().isEmpty()) {
            return Result.success(null);
        }
        return Result.success(page.getRecords().get(0));
    }

    @GetMapping("/friends")
    public Result<List<FriendLink>> getFriends() {
        return Result.success(friendLinkService.listAll());
    }

    @PostMapping("/friends/apply")
    public Result<Void> applyFriend(@RequestBody FriendLink link) {
        link.setStatus(0);
        link.setSortOrder(0);
        friendLinkService.create(link);
        return Result.success();
    }

    @PostMapping("/subscribe")
    public Result<Void> subscribe(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            throw new BadRequestException("邮箱不能为空");
        }
        subscriberService.subscribe(email.trim());
        return Result.success();
    }

    @GetMapping("/subscribe/verify")
    public Result<String> verifySubscribe(@RequestParam String token) {
        subscriberService.verify(token);
        return Result.success("订阅确认成功！");
    }

    @PostMapping("/unsubscribe")
    public Result<Void> unsubscribe(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            throw new BadRequestException("邮箱不能为空");
        }
        subscriberService.unsubscribe(email.trim());
        return Result.success();
    }
}
