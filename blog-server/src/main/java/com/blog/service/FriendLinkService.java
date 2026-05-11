package com.blog.service;

import com.blog.entity.FriendLink;
import java.util.List;

public interface FriendLinkService {
    List<FriendLink> listAll();
    List<FriendLink> listAdmin();
    void create(FriendLink link);
    void update(FriendLink link);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
}
