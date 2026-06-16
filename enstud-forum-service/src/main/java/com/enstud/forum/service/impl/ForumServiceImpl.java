package com.enstud.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.BusinessException;
import com.enstud.common.constant.ErrorCode;
import com.enstud.forum.client.UserClient;
import com.enstud.forum.dto.*;
import com.enstud.forum.entity.*;
import com.enstud.forum.mapper.*;
import com.enstud.forum.service.ForumSensitiveService;
import com.enstud.forum.service.ForumService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 论坛服务实现
 */
@Slf4j
@Service
public class ForumServiceImpl implements ForumService {

    private final ForumCategoryMapper categoryMapper;
    private final ForumPostMapper postMapper;
    private final ForumReplyMapper replyMapper;
    private final ForumLikeMapper likeMapper;
    private final UserClient userClient;
    private final ForumSensitiveService sensitiveService;

    public ForumServiceImpl(ForumCategoryMapper categoryMapper, ForumPostMapper postMapper,
                            ForumReplyMapper replyMapper, ForumLikeMapper likeMapper,
                            UserClient userClient, ForumSensitiveService sensitiveService) {
        this.categoryMapper = categoryMapper;
        this.postMapper = postMapper;
        this.replyMapper = replyMapper;
        this.likeMapper = likeMapper;
        this.userClient = userClient;
        this.sensitiveService = sensitiveService;
    }

    @Override
    public List<CategoryDTO> getCategories() {
        return categoryMapper.selectList(
                        new LambdaQueryWrapper<ForumCategory>().orderByAsc(ForumCategory::getSortOrder))
                .stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName(), c.getDescription(), c.getIcon()))
                .toList();
    }

    @Override
    public List<PostDTO> getPosts(Long categoryId, String cursor, int limit) {
        var wrapper = new LambdaQueryWrapper<ForumPost>()
                .eq(ForumPost::getStatus, "PUBLISHED")
                .orderByDesc(ForumPost::getIsPinned)
                .orderByDesc(ForumPost::getCreatedAt);
        if (categoryId != null) wrapper.eq(ForumPost::getCategoryId, categoryId);
        if (cursor != null) wrapper.lt(ForumPost::getCreatedAt, cursor);
        wrapper.last("LIMIT " + limit);

        List<ForumPost> posts = postMapper.selectList(wrapper);
        if (posts.isEmpty()) return List.of();

        // 批量查板块名
        List<Long> catIds = posts.stream().map(ForumPost::getCategoryId).distinct().toList();
        Map<Long, String> catMap = categoryMapper.selectBatchIds(catIds).stream()
                .collect(Collectors.toMap(ForumCategory::getId, ForumCategory::getName, (a, b) -> a));

        return posts.stream().map(p -> new PostDTO(
                p.getId(), p.getTitle(),
                p.getSummary() != null ? p.getSummary() : (p.getContent().length() > 100 ? p.getContent().substring(0, 100) : p.getContent()),
                p.getAuthorId(), userClient.getUserName(p.getAuthorId()), p.getCategoryId(),
                catMap.getOrDefault(p.getCategoryId(), ""),
                p.getTags(), p.getViewCount(), p.getLikeCount(), p.getReplyCount(),
                p.getIsPinned() == 1, p.getIsEssence() == 1, p.getCreatedAt()
        )).toList();
    }

    @Override
    public PostDetailDTO getPostDetail(Long postId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ErrorCode.POST_NOT_FOUND);

        // 浏览量+1
        post.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(post);

        String catName = "";
        ForumCategory cat = categoryMapper.selectById(post.getCategoryId());
        if (cat != null) catName = cat.getName();

        List<ReplyDTO> replies = getRepliesByPostId(postId);

        return new PostDetailDTO(post.getId(), post.getTitle(), post.getContent(),
                post.getAuthorId(), userClient.getUserName(post.getAuthorId()),
                post.getCategoryId(), catName,
                post.getTags(), post.getViewCount(), post.getLikeCount(),
                post.getReplyCount(), post.getIsPinned() == 1, post.getIsEssence() == 1,
                post.getCreatedAt(), replies);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDTO createPost(Long userId, CreatePostRequest request) {
        ForumCategory cat = categoryMapper.selectById(request.categoryId());
        if (cat == null) throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);

        // 敏感词检查
        String title = request.title();
        String content = request.content();
        if (sensitiveService.containsSensitive(title)) {
            throw new BusinessException(ErrorCode.CONTENT_SENSITIVE, "标题包含敏感词：" + sensitiveService.findFirstSensitive(title));
        }
        if (sensitiveService.containsSensitive(content)) {
            throw new BusinessException(ErrorCode.CONTENT_SENSITIVE, "内容包含敏感词：" + sensitiveService.findFirstSensitive(content));
        }

        ForumPost post = new ForumPost();
        post.setTitle(title);
        post.setContent(content);
        post.setSummary(content.length() > 200 ? content.substring(0, 200) : content);
        post.setAuthorId(userId);
        post.setCategoryId(request.categoryId());
        post.setTags(request.tags());
        post.setStatus("PUBLISHED");
        postMapper.insert(post);
        log.info("帖子发布成功, postId={}, userId={}", post.getId(), userId);

        return new PostDTO(post.getId(), post.getTitle(), post.getSummary(),
                userId, userClient.getUserName(userId), request.categoryId(), cat.getName(),
                request.tags(), 0, 0, 0, false, false, post.getCreatedAt());
    }

    @Override
    public void deletePost(Long userId, Long postId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        if (!post.getAuthorId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN, "只能删除自己的帖子");
        postMapper.deleteById(postId);
        log.info("帖子已删除, postId={}, userId={}", postId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReplyDTO createReply(Long userId, Long postId, CreateReplyRequest request) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(ErrorCode.POST_NOT_FOUND);

        // 敏感词检查
        String content = request.content();
        if (sensitiveService.containsSensitive(content)) {
            throw new BusinessException(ErrorCode.CONTENT_SENSITIVE, "回复内容包含敏感词：" + sensitiveService.findFirstSensitive(content));
        }

        ForumReply reply = new ForumReply();
        reply.setPostId(postId);
        reply.setContent(content);
        reply.setAuthorId(userId);
        replyMapper.insert(reply);

        // 更新帖子的回复数
        post.setReplyCount(post.getReplyCount() + 1);
        postMapper.updateById(post);

        log.info("回复成功, replyId={}, postId={}, userId={}", reply.getId(), postId, userId);
        return new ReplyDTO(reply.getId(), reply.getContent(), userId, userClient.getUserName(userId), 0, reply.getCreatedAt());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long userId, String targetType, Long targetId) {
        // 查是否已点赞
        ForumLike existing = likeMapper.selectOne(
                new LambdaQueryWrapper<ForumLike>()
                        .eq(ForumLike::getUserId, userId)
                        .eq(ForumLike::getTargetType, targetType)
                        .eq(ForumLike::getTargetId, targetId));

        if (existing != null) {
            // 取消点赞
            likeMapper.deleteById(existing.getId());
            updateLikeCount(targetType, targetId, -1);
        } else {
            // 点赞
            ForumLike like = new ForumLike();
            like.setUserId(userId);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            likeMapper.insert(like);
            updateLikeCount(targetType, targetId, 1);
        }
    }

    /**
     * 更新目标对象的点赞计数
     */
    private void updateLikeCount(String targetType, Long targetId, int delta) {
        if ("POST".equals(targetType)) {
            ForumPost post = postMapper.selectById(targetId);
            if (post != null) {
                post.setLikeCount(Math.max(0, post.getLikeCount() + delta));
                postMapper.updateById(post);
            }
        } else if ("REPLY".equals(targetType)) {
            ForumReply reply = replyMapper.selectById(targetId);
            if (reply != null) {
                reply.setLikeCount(Math.max(0, reply.getLikeCount() + delta));
                replyMapper.updateById(reply);
            }
        }
    }

    private List<ReplyDTO> getRepliesByPostId(Long postId) {
        List<ForumReply> replies = replyMapper.selectList(
                new LambdaQueryWrapper<ForumReply>()
                        .eq(ForumReply::getPostId, postId)
                        .orderByAsc(ForumReply::getCreatedAt));
        return replies.stream()
                .map(r -> new ReplyDTO(r.getId(), r.getContent(), r.getAuthorId(),
                        userClient.getUserName(r.getAuthorId()), r.getLikeCount(), r.getCreatedAt()))
                .toList();
    }
}
