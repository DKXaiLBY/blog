package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.Subscriber;
import com.blog.mapper.SubscriberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriberServiceTest {

    @Mock SubscriberMapper subscriberMapper;

    private SubscriberService subscriberService;

    private Subscriber sub;

    @BeforeEach
    void setUp() {
        subscriberService = new SubscriberService(subscriberMapper, null);
        sub = new Subscriber();
        sub.setId(1L);
        sub.setEmail("test@example.com");
        sub.setVerified(1);
        sub.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("subscribe")
    class SubscribeTests {
        @Test
        @DisplayName("should subscribe new email")
        void shouldSubscribeNewEmail() {
            when(subscriberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(subscriberMapper.insert(any(Subscriber.class))).thenReturn(1);

            subscriberService.subscribe("new@example.com");
            verify(subscriberMapper).insert(any(Subscriber.class));
        }

        @Test
        @DisplayName("should throw when email already subscribed")
        void shouldThrowWhenAlreadySubscribed() {
            when(subscriberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
            assertThrows(RuntimeException.class, () -> subscriberService.subscribe("test@example.com"));
        }
    }

    @Nested
    @DisplayName("verify")
    class VerifyTests {
        @Test
        @DisplayName("should verify subscriber with valid token")
        void shouldVerifySubscriber() {
            sub.setVerified(0);
            sub.setVerifyToken("abc12345");
            when(subscriberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sub);
            when(subscriberMapper.updateById(any(Subscriber.class))).thenReturn(1);

            subscriberService.verify("abc12345");
            assertEquals(1, sub.getVerified());
            assertNull(sub.getVerifyToken());
        }

        @Test
        @DisplayName("should throw with invalid token")
        void shouldThrowWithInvalidToken() {
            when(subscriberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            assertThrows(RuntimeException.class, () -> subscriberService.verify("invalid"));
        }
    }

    @Nested
    @DisplayName("listAll")
    class ListAllTests {
        @Test
        @DisplayName("should return verified subscribers")
        void shouldReturnVerifiedSubscribers() {
            when(subscriberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(sub));
            var result = subscriberService.listAll();
            assertEquals(1, result.size());
            assertEquals("test@example.com", result.get(0).getEmail());
        }
    }

    @Nested
    @DisplayName("unsubscribe")
    class UnsubscribeTests {
        @Test
        @DisplayName("should delete subscriber by email")
        void shouldDeleteByEmail() {
            when(subscriberMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
            subscriberService.unsubscribe("test@example.com");
            verify(subscriberMapper).delete(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("should delete subscriber by id")
        void shouldDeleteById() {
            when(subscriberMapper.deleteById(1L)).thenReturn(1);
            subscriberService.unsubscribeById(1L);
            verify(subscriberMapper).deleteById(1L);
        }
    }
}
