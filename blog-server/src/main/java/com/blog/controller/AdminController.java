package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.ArticleRequest;
import com.blog.dto.ArticleVO;
import com.blog.dto.CommentVO;
import com.blog.dto.PageVO;
import com.blog.entity.FriendLink;
import com.blog.service.ArticleService;
import com.blog.service.CommentService;
import com.blog.service.FriendLinkService;
import com.blog.service.SubscriberService;
import com.blog.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ArticleService articleService;
    private final CommentService commentService;
    private final UserService userService;
    private final FriendLinkService friendLinkService;
    private final SubscriberService subscriberService;

    public AdminController(ArticleService articleService, CommentService commentService,
                           UserService userService, FriendLinkService friendLinkService,
                           SubscriberService subscriberService) {
        this.articleService = articleService;
        this.commentService = commentService;
        this.userService = userService;
        this.friendLinkService = friendLinkService;
        this.subscriberService = subscriberService;
    }

    @GetMapping("/articles")
    public Result<PageVO<ArticleVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(articleService.listAdminArticles(page, size, keyword, status));
    }

    @GetMapping("/articles/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.getArticleDetailForAdmin(id));
    }

    @PostMapping("/articles")
    public Result<?> createArticle(@Valid @RequestBody ArticleRequest request,
                                   HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        articleService.createArticle(request, userId);
        return Result.success();
    }

    @PutMapping("/articles/{id}")
    public Result<?> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        articleService.updateArticle(id, request);
        return Result.success();
    }

    @DeleteMapping("/articles/{id}")
    public Result<?> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }

    @PostMapping("/articles/batch-delete")
    public Result<?> batchDelete(@RequestBody Map<String, List<Integer>> body) {
        List<Long> ids = body.get("ids").stream().map(Long::valueOf).toList();
        articleService.batchDelete(ids);
        return Result.success();
    }

    @PostMapping("/articles/batch-status")
    public Result<?> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = ((List<Integer>) body.get("ids")).stream().map(Long::valueOf).toList();
        Integer status = (Integer) body.get("status");
        articleService.batchUpdateStatus(ids, status);
        return Result.success();
    }

    @GetMapping("/comments")
    public Result<PageVO<CommentVO>> listComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) Integer status) {
        return Result.success(commentService.listAdminComments(page, size, articleId, status));
    }

    @DeleteMapping("/comments/{id}")
    public Result<?> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }

    @PutMapping("/comments/{id}/status")
    public Result<?> updateCommentStatus(@PathVariable Long id,
                                         @RequestParam Integer status) {
        commentService.updateCommentStatus(id, status);
        return Result.success();
    }

    @PutMapping("/user/avatar")
    public Result<?> updateAvatar(@RequestBody Map<String, String> body,
                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String avatar = body.get("avatar");
        userService.updateAvatar(userId, avatar);
        return Result.success();
    }

    @PutMapping("/user/profile")
    public Result<?> updateProfile(@RequestBody Map<String, String> body,
                                   HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.updateProfile(userId,
                body.get("nickname"),
                body.get("tagline"),
                body.get("bio"),
                body.get("skills"),
                body.get("contacts"),
                body.get("projects"),
                body.get("announcement"));
        return Result.success();
    }

    @PutMapping("/user/password")
    public Result<?> changePassword(@RequestBody Map<String, String> body,
                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.changePassword(userId,
                body.get("oldPassword"),
                body.get("newPassword"));
        return Result.success();
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.success(userService.getDashboardStats());
    }

    // === Friend Link management ===

    @GetMapping("/friends")
    public Result<?> listFriends() {
        return Result.success(friendLinkService.listAdmin());
    }

    @PostMapping("/friends")
    public Result<?> createFriend(@RequestBody FriendLink link) {
        friendLinkService.create(link);
        return Result.success();
    }

    @PutMapping("/friends/{id}")
    public Result<?> updateFriend(@PathVariable Long id, @RequestBody FriendLink link) {
        link.setId(id);
        friendLinkService.update(link);
        return Result.success();
    }

    @DeleteMapping("/friends/{id}")
    public Result<?> deleteFriend(@PathVariable Long id) {
        friendLinkService.delete(id);
        return Result.success();
    }

    @PutMapping("/friends/{id}/status")
    public Result<?> updateFriendStatus(@PathVariable Long id,
                                        @RequestParam Integer status) {
        friendLinkService.updateStatus(id, status);
        return Result.success();
    }

    // === Subscriber management ===

    @GetMapping("/subscribers")
    public Result<?> listSubscribers() {
        return Result.success(subscriberService.listAll());
    }

    @DeleteMapping("/subscribers/{id}")
    public Result<?> deleteSubscriber(@PathVariable Long id) {
        subscriberService.unsubscribeById(id);
        return Result.success();
    }
}
