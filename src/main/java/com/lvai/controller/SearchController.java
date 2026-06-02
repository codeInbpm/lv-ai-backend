package com.lvai.controller;

import com.lvai.entity.SearchHotWord;
import com.lvai.entity.TravelPlan;
import com.lvai.entity.UserNote;
import com.lvai.service.SearchHotWordService;
import com.lvai.service.UserSearchHistoryService;
import com.lvai.service.IUserNoteService;
import com.lvai.service.ITravelPlanService;
import com.lvai.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchHotWordService searchHotWordService;

    @Autowired
    private UserSearchHistoryService userSearchHistoryService;

    @Autowired
    private IUserNoteService userNoteService;

    @Autowired
    private ITravelPlanService travelPlanService;

    @GetMapping("/hot")
    public Result<List<SearchHotWord>> getHotWords(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(searchHotWordService.getHotWords(limit));
    }

    @GetMapping("/history")
    public Result<List<String>> getHistory(@RequestParam Long userId, @RequestParam(defaultValue = "10") int limit) {
        // In a real app, userId should be obtained from JWT/Session
        return Result.success(userSearchHistoryService.getRecentSearchHistory(userId, limit));
    }

    @PostMapping("/history/add")
    public Result<Void> addHistory(@RequestParam Long userId, @RequestParam String keyword) {
        userSearchHistoryService.addSearchHistory(userId, keyword);
        return Result.success(null);
    }

    @PostMapping("/history/clear")
    public Result<Void> clearHistory(@RequestParam Long userId) {
        userSearchHistoryService.clearSearchHistory(userId);
        return Result.success(null);
    }

    @GetMapping("/notes")
    public Result<List<UserNote>> searchNotes(@RequestParam String keyword) {
        List<UserNote> notes = userNoteService.lambdaQuery()
                .like(UserNote::getTitle, keyword).or().like(UserNote::getContent, keyword)
                .orderByDesc(UserNote::getCreateTime)
                .last("limit 10")
                .list();
        return Result.success(notes);
    }

    @GetMapping("/plans")
    public Result<List<TravelPlan>> searchPlans(@RequestParam String keyword) {
        List<TravelPlan> plans = travelPlanService.lambdaQuery()
                .eq(TravelPlan::getIsPublic, 1)
                .and(wrapper -> wrapper.like(TravelPlan::getTitle, keyword).or().like(TravelPlan::getDestination, keyword))
                .orderByDesc(TravelPlan::getCreateTime)
                .last("limit 10")
                .list();
        return Result.success(plans);
    }
}
