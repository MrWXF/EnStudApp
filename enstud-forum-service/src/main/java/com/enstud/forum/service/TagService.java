package com.enstud.forum.service;

import com.enstud.forum.dto.PostDTO;
import com.enstud.forum.dto.TagDTO;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {

    /**
     * 获取热门标签
     */
    List<TagDTO> getHotTags(int limit);

    /**
     * 获取标签下的帖子列表
     */
    List<PostDTO> getPostsByTag(Long tagId, String cursor, int limit);

    /**
     * 为帖子添加标签
     */
    void addTagsToPost(Long postId, List<String> tagNames);

    /**
     * 根据标签名查找或创建标签
     */
    Long findOrCreateTag(String tagName);
}
