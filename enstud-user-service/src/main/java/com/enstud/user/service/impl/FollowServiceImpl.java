package com.enstud.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.BusinessException;
import com.enstud.common.constant.ErrorCode;
import com.enstud.common.entity.User;
import com.enstud.common.entity.UserFollow;
import com.enstud.user.dto.FollowDTO;
import com.enstud.user.dto.FollowStatusDTO;
import com.enstud.user.mapper.UserFollowMapper;
import com.enstud.user.mapper.UserMapper;
import com.enstud.user.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final UserFollowMapper followMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followerId, Long followingId) {
        // 不能关注自己
        if (followerId.equals(followingId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能关注自己");
        }

        // 检查是否已关注
        UserFollow existing = followMapper.selectOne(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, followerId)
                        .eq(UserFollow::getFollowingId, followingId)
        );

        if (existing != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已经关注该用户");
        }

        // 检查目标用户是否存在
        User targetUser = userMapper.selectById(followingId);
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 创建关注记录
        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        followMapper.insert(follow);

        // 更新冗余字段
        updateUserFollowCounts(followerId, followingId);

        log.info("用户关注: followerId={}, followingId={}", followerId, followingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long followerId, Long followingId) {
        // 删除关注记录
        int deleted = followMapper.delete(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, followerId)
                        .eq(UserFollow::getFollowingId, followingId)
        );

        if (deleted == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "未关注该用户");
        }

        // 更新冗余字段
        updateUserFollowCounts(followerId, followingId);

        log.info("用户取消关注: followerId={}, followingId={}", followerId, followingId);
    }

    @Override
    public List<FollowDTO> getFollowing(Long userId, String cursor, int limit) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .orderByDesc(UserFollow::getCreatedAt);

        if (cursor != null) {
            wrapper.lt(UserFollow::getId, Long.parseLong(cursor));
        }
        wrapper.last("LIMIT " + limit);

        List<UserFollow> follows = followMapper.selectList(wrapper);
        if (follows.isEmpty()) {
            return List.of();
        }

        List<Long> followingIds = follows.stream().map(UserFollow::getFollowingId).toList();
        List<User> users = userMapper.selectBatchIds(followingIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        return follows.stream()
                .map(f -> {
                    User user = userMap.get(f.getFollowingId());
                    if (user == null) return null;
                    return new FollowDTO(
                            user.getId(),
                            user.getUsername(),
                            user.getNickname(),
                            user.getAvatarUrl()
                    );
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<FollowDTO> getFollowers(Long userId, String cursor, int limit) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowingId, userId)
                .orderByDesc(UserFollow::getCreatedAt);

        if (cursor != null) {
            wrapper.lt(UserFollow::getId, Long.parseLong(cursor));
        }
        wrapper.last("LIMIT " + limit);

        List<UserFollow> follows = followMapper.selectList(wrapper);
        if (follows.isEmpty()) {
            return List.of();
        }

        List<Long> followerIds = follows.stream().map(UserFollow::getFollowerId).toList();
        List<User> users = userMapper.selectBatchIds(followerIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        return follows.stream()
                .map(f -> {
                    User user = userMap.get(f.getFollowerId());
                    if (user == null) return null;
                    return new FollowDTO(
                            user.getId(),
                            user.getUsername(),
                            user.getNickname(),
                            user.getAvatarUrl()
                    );
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public FollowStatusDTO getFollowStatus(Long userId, Long targetUserId) {
        UserFollow follow = followMapper.selectOne(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, userId)
                        .eq(UserFollow::getFollowingId, targetUserId)
        );

        return new FollowStatusDTO(follow != null);
    }

    private void updateUserFollowCounts(Long followerId, Long followingId) {
        // 更新关注者的 following_count
        Long followingCount = followMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, followerId)
        );
        User follower = userMapper.selectById(followerId);
        if (follower != null) {
            follower.setFollowingCount(followingCount.intValue());
            userMapper.updateById(follower);
        }

        // 更新被关注者的 follower_count
        Long followerCount = followMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowingId, followingId)
        );
        User following = userMapper.selectById(followingId);
        if (following != null) {
            following.setFollowerCount(followerCount.intValue());
            userMapper.updateById(following);
        }
    }
}
