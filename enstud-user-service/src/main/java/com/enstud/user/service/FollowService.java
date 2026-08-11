package com.enstud.user.service;

import com.enstud.user.dto.FollowDTO;
import com.enstud.user.dto.FollowStatusDTO;

import java.util.List;

/**
 * 用户关注服务接口
 */
public interface FollowService {

    /**
     * 关注用户
     */
    void follow(Long followerId, Long followingId);

    /**
     * 取消关注
     */
    void unfollow(Long followerId, Long followingId);

    /**
     * 获取我关注的用户列表
     */
    List<FollowDTO> getFollowing(Long userId, String cursor, int limit);

    /**
     * 获取我的粉丝列表
     */
    List<FollowDTO> getFollowers(Long userId, String cursor, int limit);

    /**
     * 查询是否已关注某用户
     */
    FollowStatusDTO getFollowStatus(Long userId, Long targetUserId);
}
