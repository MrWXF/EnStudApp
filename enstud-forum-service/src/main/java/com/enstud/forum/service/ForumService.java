package com.enstud.forum.service;

import com.enstud.forum.dto.*;

import java.util.List;

public interface ForumService {
    List<CategoryDTO> getCategories();
    List<PostDTO> getPosts(Long categoryId, String cursor, int limit);
    PostDetailDTO getPostDetail(Long postId);
    PostDTO createPost(Long userId, CreatePostRequest request);
    void deletePost(Long userId, Long postId);
    ReplyDTO createReply(Long userId, Long postId, CreateReplyRequest request);
    void toggleLike(Long userId, String targetType, Long targetId);
}
