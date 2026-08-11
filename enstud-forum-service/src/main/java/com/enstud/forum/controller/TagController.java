package com.enstud.forum.controller;

import com.enstud.common.Result;
import com.enstud.forum.dto.PostDTO;
import com.enstud.forum.dto.TagDTO;
import com.enstud.forum.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "论坛标签", description = "标签查询、按标签筛选帖子")
@RestController
@RequestMapping("/forum/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "获取热门标签")
    @GetMapping
    public Result<List<TagDTO>> getHotTags(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.success(tagService.getHotTags(limit));
    }

    @Operation(summary = "获取标签下的帖子列表")
    @GetMapping("/{tagId}/posts")
    public Result<List<PostDTO>> getPostsByTag(
            @PathVariable Long tagId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return Result.success(tagService.getPostsByTag(tagId, cursor, limit));
    }
}
