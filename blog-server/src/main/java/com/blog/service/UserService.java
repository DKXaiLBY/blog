package com.blog.service;

import com.blog.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    String login(String username, String password);
    User getById(Long id);
    List<User> listAll();
    void updateAvatar(Long id, String avatar);
    void updateProfile(Long id, String nickname, String tagline, String bio, String skills, String contacts, String projects, String announcement);
    void changePassword(Long id, String oldPassword, String newPassword);
    Map<String, Object> getDashboardStats();
}
