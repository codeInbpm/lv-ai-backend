package com.lvai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lvai.common.Result;
import com.lvai.dto.CommunityStatsDTO;
import com.lvai.service.ICommunityService;
import com.lvai.service.IUserBrowsingHistoryService;
import com.lvai.service.IContentDraftService;
import com.lvai.service.IFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "社区互动管理", description = "获取收藏、草稿、历史记录等信息")
@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final ICommunityService communityService;
    private final IUserBrowsingHistoryService historyService;
    private final IContentDraftService draftService;
    private final IFileService fileService;

    @GetMapping("/stats")
    @Operation(summary = "获取用户社区数据统计(关注/粉丝/获赞)")
    public Result<CommunityStatsDTO> getCommunityStats() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(communityService.getUserCommunityStats(userId));
    }

    @GetMapping("/collections/{type}")
    @Operation(summary = "获取用户收藏列表(1:笔记, 3:攻略)")
    public Result<List<com.lvai.vo.UserCollectionVO>> getCollections(@PathVariable Integer type) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(communityService.getCollections(userId, type));
    }

    @GetMapping("/collections")
    @Operation(summary = "获取用户全部收藏列表(混合1:笔记, 3:攻略, 4:景区)")
    public Result<List<com.lvai.vo.UserCollectionVO>> getAllCollections() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(communityService.getAllCollections(userId));
    }

    @GetMapping("/history")
    @Operation(summary = "获取用户浏览历史")
    public Result<java.util.List<com.lvai.entity.UserBrowsingHistory>> getBrowsingHistory() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(historyService.list(
                new LambdaQueryWrapper<com.lvai.entity.UserBrowsingHistory>()
                        .eq(com.lvai.entity.UserBrowsingHistory::getUserId, userId)
                        .orderByDesc(com.lvai.entity.UserBrowsingHistory::getViewTime)
        ));
    }

    @GetMapping("/drafts")
    @Operation(summary = "获取用户草稿箱")
    public Result<java.util.List<com.lvai.entity.ContentDraft>> getDrafts() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(draftService.list(
                new LambdaQueryWrapper<com.lvai.entity.ContentDraft>()
                        .eq(com.lvai.entity.ContentDraft::getUserId, userId)
                        .orderByDesc(com.lvai.entity.ContentDraft::getUpdateTime)
        ));
    }

    @PostMapping("/drafts/save")
    @Operation(summary = "保存或更新草稿")
    public Result<Void> saveDraft(@RequestBody com.lvai.entity.ContentDraft draft) {
        Long userId = StpUtil.getLoginIdAsLong();
        draft.setUserId(userId);
        draftService.saveOrUpdate(draft);
        return Result.success();
    }

    @DeleteMapping("/drafts/{id}")
    @Operation(summary = "删除草稿并清理MinIO文件")
    public Result<Void> deleteDraft(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        com.lvai.entity.ContentDraft draft = draftService.getOne(
                new LambdaQueryWrapper<com.lvai.entity.ContentDraft>()
                        .eq(com.lvai.entity.ContentDraft::getId, id)
                        .eq(com.lvai.entity.ContentDraft::getUserId, userId)
        );
        
        if (draft != null) {
            // 解析草稿内容中的图片并删除
            try {
                JSONObject contentObj = JSON.parseObject(draft.getContent());
                // 假设 JSON 中有 coverUrl 或 images 数组
                if (contentObj.containsKey("coverUrl")) {
                    fileService.deleteFile(contentObj.getString("coverUrl"));
                }
                if (contentObj.containsKey("images")) {
                    List<String> images = contentObj.getList("images", String.class);
                    if (images != null) {
                        images.forEach(fileService::deleteFile);
                    }
                }
            } catch (Exception e) {
                // 解析失败不影响删除草稿记录
            }
            draftService.removeById(id);
        }
        return Result.success();
    }

    @GetMapping("/drafts/{id}")
    @Operation(summary = "获取草稿详情")
    public Result<com.lvai.entity.ContentDraft> getDraftDetail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(draftService.getOne(
                new LambdaQueryWrapper<com.lvai.entity.ContentDraft>()
                        .eq(com.lvai.entity.ContentDraft::getId, id)
                        .eq(com.lvai.entity.ContentDraft::getUserId, userId)
        ));
    }
}
