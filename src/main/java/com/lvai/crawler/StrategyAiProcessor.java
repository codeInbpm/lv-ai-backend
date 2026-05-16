package com.lvai.crawler;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.lvai.entity.StrategyPost;
import com.lvai.entity.StrategyTag;
import com.lvai.entity.StrategyPostTag;
import com.lvai.service.IStrategyPostService;
import com.lvai.service.IStrategyTagService;
import com.lvai.service.IStrategyPostTagService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class StrategyAiProcessor {

    @Value("${ai.provider}")
    private String aiProvider;

    private final ChatModel openAiChatModel;
    private final ChatModel dashscopeChatModel;
    private final ChatModel deepSeekChatModel;
    private final IStrategyPostService postService;
    private final IStrategyTagService tagService;
    private final IStrategyPostTagService postTagService;

    public StrategyAiProcessor(@Qualifier("openAiChatModel") ChatModel openAiChatModel,
                               @Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel,
                               @Qualifier("deepSeekChatModel") ChatModel deepSeekChatModel,
                               IStrategyPostService postService,
                               IStrategyTagService tagService,
                               IStrategyPostTagService postTagService) {
        this.openAiChatModel = openAiChatModel;
        this.dashscopeChatModel = dashscopeChatModel;
        this.deepSeekChatModel = deepSeekChatModel;
        this.postService = postService;
        this.tagService = tagService;
        this.postTagService = postTagService;
    }

    private ChatModel getChatModel() {
        return switch (aiProvider.toLowerCase()) {
            case "qwen" -> dashscopeChatModel;
            case "deepseek" -> deepSeekChatModel;
            default -> openAiChatModel;
        };
    }

    public void processAsync(StrategyPost post) {
        new Thread(() -> {
            try {
                log.info("开始对攻略进行 AI 增强处理: {}", post.getTitle());
                String promptText = "请阅读以下旅游攻略原文，提取并输出 JSON 格式（无需 markdown 代码块）：\n" +
                        "{\"summary\":\"200字核心摘要\", \"tags\":[\"标签1\", \"标签2\"], \"itinerary\":[{\"day\":\"Day1\",\"desc\":\"行程描述\"}]}\n\n" +
                        "原文内容：" + post.getContent();
                
                ChatResponse response = getChatModel().call(new Prompt(List.of(
                        new SystemMessage("你是一个专业的旅游内容分析 AI。"),
                        new UserMessage(promptText)
                )));
                
                String resText = response.getResult().getOutput().getText();
                // 简单提取 JSON
                int start = resText.indexOf("{");
                int end = resText.lastIndexOf("}");
                if(start >=0 && end > start) {
                    resText = resText.substring(start, end+1);
                    JSONObject json = JSON.parseObject(resText);
                    
                    post.setAiSummary(json.getString("summary"));
                    post.setAiItinerary(json.getJSONArray("itinerary").toJSONString());
                    postService.updateById(post);
                    
                    // 处理标签
                    List<String> tags = json.getJSONArray("tags").toJavaList(String.class);
                    if (tags != null) {
                        for(String t : tags) {
                            StrategyTag tag = tagService.getOne(new LambdaQueryWrapper<StrategyTag>().eq(StrategyTag::getName, t));
                            if (tag == null) {
                                tag = new StrategyTag();
                                tag.setName(t);
                                tag.setType(0);
                                tagService.save(tag);
                            }
                            StrategyPostTag pt = new StrategyPostTag();
                            pt.setPostId(post.getId());
                            pt.setTagId(tag.getId());
                            try {
                                postTagService.save(pt);
                            } catch (Exception ignored) {} // ignore duplicates
                        }
                    }
                }
                log.info("AI 增强处理完成: {}", post.getTitle());
            } catch (Exception e) {
                log.error("AI 处理攻略失败", e);
            }
        }).start();
    }
}