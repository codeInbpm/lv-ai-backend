package com.lvai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvai.common.Result;
import com.lvai.entity.Topic;
import com.lvai.entity.TopicCategory;
import com.lvai.mapper.TopicCategoryMapper;
import com.lvai.mapper.TopicMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.lvai.dto.TopicCreateDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "话题模块")
@RestController
@RequestMapping("/topic")
@RequiredArgsConstructor
public class TopicController {

    private final TopicMapper topicMapper;
    private final TopicCategoryMapper categoryMapper;

    @GetMapping("/categories")
    @Operation(summary = "获取话题分类列表")
    public Result<List<TopicCategory>> getCategories() {
        return Result.success(categoryMapper.selectList(
                new LambdaQueryWrapper<TopicCategory>().orderByAsc(TopicCategory::getSort)
        ));
    }

    @GetMapping("/list")
    @Operation(summary = "获取话题列表")
    public Result<List<Topic>> getTopics(@RequestParam(required = false) Long categoryId, 
                                        @RequestParam(required = false) String keyword) {
        return Result.success(topicMapper.selectList(
                new LambdaQueryWrapper<Topic>()
                        .eq(categoryId != null, Topic::getCategoryId, categoryId)
                        .like(keyword != null, Topic::getTitle, keyword)
                        .orderByDesc(Topic::getFollowerCount)
        ));
    }

    @PostMapping("/save-or-get")
    @Operation(summary = "获取或创建话题")
    public Result<Topic> saveOrGetTopic(@RequestBody TopicCreateDTO dto) {
        // 去掉 # 前缀
        String finalTitle = dto.getTitle().replace("#", "").trim();
        Topic existing = topicMapper.selectOne(new LambdaQueryWrapper<Topic>().eq(Topic::getTitle, finalTitle));
        if (existing != null) {
            return Result.success(existing);
        }
        
        Topic newTopic = new Topic();
        newTopic.setTitle(finalTitle);
        newTopic.setCategoryId(0L); // 默认未分类
        newTopic.setFollowerCount(0);
        newTopic.setContentCount(0);
        topicMapper.insert(newTopic);
        return Result.success(newTopic);
    }
}
