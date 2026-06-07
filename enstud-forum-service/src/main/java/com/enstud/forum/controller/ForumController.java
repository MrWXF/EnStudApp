package com.enstud.forum.controller;

import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.forum.dto.*;
import com.enstud.forum.service.ForumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "论坛交流", description = "板块、帖子、回复、点赞")
@RestController
@RequestMapping("/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    @Operation(summary = "获取板块列表")
    @GetMapping("/categories")
    public Result<List<CategoryDTO>> getCategories() {
        return Result.success(forumService.getCategories());
    }

    @Operation(summary = "获取帖子列表")
    @GetMapping("/posts")
    public Result<List<PostDTO>> getPosts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return Result.success(forumService.getPosts(categoryId, cursor, limit));
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("/posts/{postId}")
    public Result<PostDetailDTO> getPostDetail(@PathVariable Long postId) {
        return Result.success(forumService.getPostDetail(postId));
    }

    @Operation(summary = "发布帖子")
    @PostMapping("/posts")
    public Result<PostDTO> createPost(@Valid @RequestBody CreatePostRequest request) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        return Result.success(forumService.createPost(userId, request));
    }

    @Operation(summary = "删除帖子")
    @DeleteMapping("/posts/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        forumService.deletePost(userId, postId);
        return Result.success();
    }

    @Operation(summary = "回复帖子")
    @PostMapping("/posts/{postId}/reply")
    public Result<ReplyDTO> reply(@PathVariable Long postId, @Valid @RequestBody CreateReplyRequest request) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        return Result.success(forumService.createReply(userId, postId, request));
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/like")
    public Result<Void> toggleLike(@RequestParam String targetType, @RequestParam Long targetId) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        forumService.toggleLike(userId, targetType, targetId);
        return Result.success();
    }
}
