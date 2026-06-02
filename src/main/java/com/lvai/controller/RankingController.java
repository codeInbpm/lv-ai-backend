package com.lvai.controller;

import com.lvai.entity.RankingList;
import com.lvai.entity.RankingListItem;
import com.lvai.service.RankingListService;
import com.lvai.service.RankingListItemService;
import com.lvai.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ranking")
public class RankingController {

    @Autowired
    private RankingListService rankingListService;

    @Autowired
    private RankingListItemService rankingListItemService;

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getRankingDetail(@PathVariable Long id) {
        RankingList ranking = rankingListService.getById(id);
        if (ranking == null) {
            return Result.error("Ranking not found");
        }
        List<RankingListItem> items = rankingListItemService.getItemsByRankingId(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("ranking", ranking);
        response.put("items", items);
        
        return Result.success(response);
    }
}
