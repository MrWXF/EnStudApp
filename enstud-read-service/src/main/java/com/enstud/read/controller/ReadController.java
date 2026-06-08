package com.enstud.read.controller;

import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.read.dto.ArticleDTO;
import com.enstud.read.dto.ArticleDetailDTO;
import com.enstud.read.dto.SourceDTO;
import com.enstud.read.service.ReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "阅读服务", description = "热门英文技术文章聚合、阅读、翻译")
@RestController
@RequestMapping("/read")
public class ReadController {

    private final ReadService readService;

    public ReadController(ReadService readService) {
        this.readService = readService;
    }

    @Operation(summary = "获取热门文章列表")
    @GetMapping("/hot")
    public Result<List<ArticleDTO>> getHotArticles(
            @Parameter(description = "文章来源筛选 (HN/GitHub/Medium/all)")
            @RequestParam(required = false, defaultValue = "all") String source) {
        Long userId = SecurityContext.getCurrentUserId();
        return Result.success(readService.getHotArticles(userId, source));
    }

    @Operation(summary = "查看文章详情")
    @GetMapping("/{id}")
    public Result<ArticleDetailDTO> getArticleDetail(@PathVariable Long id) {
        Long userId = SecurityContext.getCurrentUserId();
        ArticleDetailDTO dto = readService.getArticleDetail(userId, id);
        if (dto == null) {
            return Result.fail(404, "文章不存在");
        }
        return Result.success(dto);
    }

    @Operation(summary = "获取文章中文翻译（懒加载）")
    @GetMapping("/{id}/translate")
    public Result<ArticleDetailDTO> getTranslation(@PathVariable Long id) {
        ArticleDetailDTO dto = readService.getArticleTranslation(id);
        if (dto == null) {
            return Result.fail(404, "文章不存在");
        }
        return Result.success(dto);
    }

    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/{id}/bookmark")
    public Result<Boolean> toggleBookmark(@PathVariable Long id) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        return Result.success(readService.toggleBookmark(userId, id));
    }

    @Operation(summary = "获取收藏列表")
    @GetMapping("/bookmarks")
    public Result<List<ArticleDTO>> getBookmarks() {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        return Result.success(readService.getBookmarks(userId));
    }

    @Operation(summary = "获取文章来源列表")
    @GetMapping("/sources")
    public Result<List<SourceDTO>> getSources() {
        Long userId = SecurityContext.getCurrentUserId();
        return Result.success(readService.getSources(userId));
    }

    @Operation(summary = "手动触发文章同步")
    @PostMapping("/sync")
    public Result<Integer> sync() {
        int count = readService.syncArticles();
        return Result.success(count);
    }
}
