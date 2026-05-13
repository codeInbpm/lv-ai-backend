package com.lvai.job;
import com.lvai.crawler.IStrategyCrawler;
import com.lvai.crawler.StrategyAiProcessor;
import com.lvai.entity.CrawlerTask;
import com.lvai.entity.StrategyPost;
import com.lvai.service.ICrawlerTaskService;
import com.lvai.service.IStrategyPostService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlerJobHandler {
    private final ICrawlerTaskService taskService;
    private final IStrategyPostService postService;
    private final StrategyAiProcessor aiProcessor;
    private final List<IStrategyCrawler> crawlers;

    @XxlJob("strategyCrawlerJob")
    public void execute() {
        XxlJobHelper.log("开始执行攻略采集定时任务...");
        Map<String, IStrategyCrawler> crawlerMap = crawlers.stream().collect(Collectors.toMap(IStrategyCrawler::getSource, c -> c));
        
        List<CrawlerTask> tasks = taskService.list();
        for (CrawlerTask task : tasks) {
            if (task.getStatus() == 0) continue;
            
            IStrategyCrawler crawler = crawlerMap.get(task.getSource());
            if (crawler == null) {
                XxlJobHelper.log("找不到对应的爬虫实现: " + task.getSource());
                continue;
            }
            
            List<StrategyPost> posts = crawler.crawl(task.getKeyword(), task.getMaxPages());
            for (StrategyPost post : posts) {
                try {
                    postService.save(post);
                    // 触发 AI 分析
                    aiProcessor.processAsync(post);
                    XxlJobHelper.log("成功采集并入库: " + post.getTitle());
                } catch (Exception e) {
                    XxlJobHelper.log("重复入库跳过: " + post.getExternalId());
                }
            }
            task.setLastRunTime(LocalDateTime.now());
            taskService.updateById(task);
        }
        XxlJobHelper.log("任务执行完毕");
    }
}