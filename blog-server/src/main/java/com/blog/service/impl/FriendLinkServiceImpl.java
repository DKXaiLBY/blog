package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.exception.NotFoundException;
import com.blog.entity.FriendLink;
import com.blog.mapper.FriendLinkMapper;
import com.blog.service.FriendLinkService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendLinkServiceImpl implements FriendLinkService {

    private final FriendLinkMapper friendLinkMapper;

    public FriendLinkServiceImpl(FriendLinkMapper friendLinkMapper) {
        this.friendLinkMapper = friendLinkMapper;
    }

    @Override
    public List<FriendLink> listAll() {
        LambdaQueryWrapper<FriendLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendLink::getStatus, 1);
        wrapper.orderByAsc(FriendLink::getSortOrder);
        return friendLinkMapper.selectList(wrapper);
    }

    @Override
    public List<FriendLink> listAdmin() {
        LambdaQueryWrapper<FriendLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FriendLink::getSortOrder);
        return friendLinkMapper.selectList(wrapper);
    }

    @Override
    public void create(FriendLink link) {
        friendLinkMapper.insert(link);
    }

    @Override
    public void update(FriendLink link) {
        friendLinkMapper.updateById(link);
    }

    @Override
    public void delete(Long id) {
        friendLinkMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        FriendLink link = friendLinkMapper.selectById(id);
        if (link == null) {
            throw new NotFoundException("友链不存在");
        }
        link.setStatus(status);
        friendLinkMapper.updateById(link);
    }
}
