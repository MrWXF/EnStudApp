package com.enstud.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.forum.client.UserClient;
import com.enstud.forum.dto.PostDTO;
import com.enstud.forum.dto.TagDTO;
import com.enstud.forum.entity.*;
import com.enstud.forum.mapper.*;
import com.enstud.forum.service.TagService;
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
public class TagServiceImpl implements TagService {

    private final ForumTagMapper tagMapper;
    private final ForumPostTagMapper postTagMapper;
    private final ForumPostMapper postMapper;
    private final ForumCategoryMapper categoryMapper;
    private final UserClient userClient;

    @Override
    public List<TagDTO> getHotTags(int limit) {
        List<ForumTag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<ForumTag>()
                        .orderByDesc(ForumTag::getPostCount)
                        .last("LIMIT " + limit)
        );

        return tags.stream()
                .map(t -> new TagDTO(t.getId(), t.getName(), t.getPostCount()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getPostsByTag(Long tagId, String cursor, int limit) {
        // 查询标签关联的帖子ID
        List<ForumPostTag> postTags = postTagMapper.selectList(
                new LambdaQueryWrapper<ForumPostTag>()
                        .eq(ForumPostTag::getTagId, tagId)
                        .orderByDesc(ForumPostTag::getCreatedAt)
        );

        if (postTags.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = postTags.stream()
                .map(ForumPostTag::getPostId)
                .collect(Collectors.toList());

        // 游标分页
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<ForumPost>()
                .in(ForumPost::getId, postIds)
                .eq(ForumPost::getStatus, "PUBLISHED")
                .orderByDesc(ForumPost::getIsPinned)
                .orderByDesc(ForumPost::getCreatedAt);

        if (cursor != null) {
            wrapper.lt(ForumPost::getCreatedAt, cursor);
        }
        wrapper.last("LIMIT " + limit);

        List<ForumPost> posts = postMapper.selectList(wrapper);
        if (posts.isEmpty()) {
            return List.of();
        }

        // 批量查询板块名
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
        )).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTagsToPost(Long postId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        for (String tagName : tagNames) {
            if (tagName == null || tagName.isBlank()) {
                continue;
            }

            // 查找或创建标签
            Long tagId = findOrCreateTag(tagName.trim());

            // 创建关联
            ForumPostTag postTag = new ForumPostTag();
            postTag.setPostId(postId);
            postTag.setTagId(tagId);
            postTagMapper.insert(postTag);

            // 更新标签的帖子计数
            ForumTag tag = tagMapper.selectById(tagId);
            if (tag != null) {
                tag.setPostCount(tag.getPostCount() + 1);
                tagMapper.updateById(tag);
            }
        }

        log.info("为帖子添加标签: postId={}, tags={}", postId, tagNames);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long findOrCreateTag(String tagName) {
        // 查找已存在的标签
        ForumTag tag = tagMapper.selectOne(
                new LambdaQueryWrapper<ForumTag>()
                        .eq(ForumTag::getName, tagName)
                        .last("LIMIT 1")
        );

        if (tag != null) {
            return tag.getId();
        }

        // 创建新标签
        tag = new ForumTag();
        tag.setName(tagName);
        tag.setPostCount(0);
        tagMapper.insert(tag);

        return tag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTagsOfPost(Long postId) {
        List<ForumPostTag> postTags = postTagMapper.selectList(
                new LambdaQueryWrapper<ForumPostTag>()
                        .eq(ForumPostTag::getPostId, postId)
        );
        if (postTags.isEmpty()) {
            return;
        }

        for (ForumPostTag pt : postTags) {
            ForumTag tag = tagMapper.selectById(pt.getTagId());
            if (tag != null && tag.getPostCount() > 0) {
                tag.setPostCount(tag.getPostCount() - 1);
                tagMapper.updateById(tag);
            }
            postTagMapper.delete(
                    new LambdaQueryWrapper<ForumPostTag>()
                            .eq(ForumPostTag::getPostId, postId)
                            .eq(ForumPostTag::getTagId, pt.getTagId())
            );
        }
        log.info("已移除帖子标签关联: postId={}, 关联数={}", postId, postTags.size());
    }
}
