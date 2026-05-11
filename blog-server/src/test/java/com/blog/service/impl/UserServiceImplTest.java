package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.JwtUtils;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.User;
import com.blog.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserMapper userMapper;
    @Mock ArticleMapper articleMapper;
    @Mock CommentMapper commentMapper;
    @Mock CategoryMapper categoryMapper;
    @Mock TagMapper tagMapper;

    private JwtUtils jwtUtils;
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        // Use real JwtUtils instead of mock (Mockito can't mock it on Java 25)
        jwtUtils = new JwtUtils("test-secret-key-for-jwt-testing-must-be-long-enough", 3600000);

        userService = new UserServiceImpl(userMapper, jwtUtils, articleMapper,
                commentMapper, categoryMapper, tagMapper);

        user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
        user.setNickname("博主");
        user.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
    }

    @Nested
    @DisplayName("login")
    class LoginTests {
        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("should return token on successful login")
        void shouldReturnTokenOnSuccess() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

            String token = userService.login("admin", "admin123");

            assertNotNull(token);
            assertFalse(token.isBlank());
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("should throw when username not found")
        void shouldThrowWhenUsernameNotFound() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            assertThrows(RuntimeException.class, () -> userService.login("nobody", "pass"));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("should throw when password is wrong")
        void shouldThrowWhenPasswordWrong() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
            assertThrows(RuntimeException.class, () -> userService.login("admin", "wrong"));
        }
    }

    @Nested
    @DisplayName("updateAvatar")
    class UpdateAvatarTests {
        @Test
        @DisplayName("should update avatar url")
        void shouldUpdateAvatarUrl() {
            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            userService.updateAvatar(1L, "/uploads/new-avatar.jpg");
            assertEquals("/uploads/new-avatar.jpg", user.getAvatar());
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);
            assertThrows(RuntimeException.class, () -> userService.updateAvatar(999L, "x"));
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfileTests {
        @Test
        @DisplayName("should update profile fields")
        void shouldUpdateProfileFields() {
            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            userService.updateProfile(1L, "新昵称", "一句话", "介绍", "Java|Vue", "github.com", null, null);

            assertEquals("新昵称", user.getNickname());
            assertEquals("一句话", user.getTagline());
            assertEquals("介绍", user.getBio());
            assertEquals("Java|Vue", user.getSkills());
            assertEquals("github.com", user.getContacts());
        }

        @Test
        @DisplayName("should only update non-null fields")
        void shouldOnlyUpdateNonNullFields() {
            user.setNickname("Original");
            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            userService.updateProfile(1L, null, "new tagline", null, null, null, null, null);

            assertEquals("Original", user.getNickname()); // unchanged
            assertEquals("new tagline", user.getTagline()); // changed
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePasswordTests {
        @Test
        @DisplayName("should change password when old password is correct")
        void shouldChangePassword() {
            when(userMapper.selectById(1L)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            userService.changePassword(1L, "admin123", "newpass");

            assertTrue(BCrypt.checkpw("newpass", user.getPassword()));
        }

        @Test
        @DisplayName("should throw when old password is wrong")
        void shouldThrowWhenOldPasswordWrong() {
            when(userMapper.selectById(1L)).thenReturn(user);
            assertThrows(RuntimeException.class,
                    () -> userService.changePassword(1L, "wrongold", "newpass"));
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);
            assertThrows(RuntimeException.class,
                    () -> userService.changePassword(999L, "old", "new"));
        }
    }

    @Nested
    @DisplayName("getDashboardStats")
    class GetDashboardStatsTests {
        @Test
        @DisplayName("should return all dashboard statistics")
        void shouldReturnDashboardStats() {
            when(articleMapper.selectCount(isNull())).thenReturn(10L);
            when(articleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
            when(commentMapper.selectCount(isNull())).thenReturn(25L);
            when(categoryMapper.selectCount(isNull())).thenReturn(5L);
            when(tagMapper.selectCount(isNull())).thenReturn(8L);

            Article a1 = new Article();
            a1.setId(1L);
            a1.setTitle("Article 1");
            a1.setStatus(1);
            a1.setViewCount(100);
            a1.setLikeCount(10);
            a1.setCategoryId(1L);
            a1.setContent("Some content here");
            a1.setCreatedAt(LocalDateTime.now().minusDays(1));

            Article a2 = new Article();
            a2.setId(2L);
            a2.setTitle("Article 2");
            a2.setStatus(0);
            a2.setViewCount(0);
            a2.setLikeCount(0);
            a2.setCreatedAt(LocalDateTime.now().minusDays(1));

            when(articleMapper.selectList(null)).thenReturn(List.of(a1, a2));

            Category cat = new Category();
            cat.setId(1L);
            cat.setName("Tech");
            when(categoryMapper.selectList(null)).thenReturn(List.of(cat));

            Map<String, Object> stats = userService.getDashboardStats();

            assertEquals(10L, stats.get("totalArticles"));
            assertEquals(1L, stats.get("publishedArticles"));
            assertEquals(9L, stats.get("draftArticles"));
            assertEquals(25L, stats.get("totalComments"));
            assertEquals(5L, stats.get("totalCategories"));
            assertEquals(8L, stats.get("totalTags"));
            assertEquals(100L, stats.get("totalViews"));
            assertEquals(10L, stats.get("totalLikes"));
            assertNotNull(stats.get("monthlyTrend"));
            assertNotNull(stats.get("topArticles"));
            assertNotNull(stats.get("avgViews"));
            assertNotNull(stats.get("topCategory"));
        }
    }
}
