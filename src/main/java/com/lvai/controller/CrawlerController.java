package com.lvai.controller;
import com.lvai.common.Result;
import com.lvai.job.CrawlerJobHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawler")
@RequiredArgsConstructor
@Tag(name = "爬虫任务管理")
public class CrawlerController {

    private final CrawlerJobHandler crawlerJobHandler;

    @PostMapping("/trigger")
    @Operation(summary = "手动触发全网爬虫聚合任务")
    public Result<String> triggerCrawler() {
        // 由于 XXL-Job 的 Helper 可能在非 XXL 线程中调用报错，
        // 我们在新线程中执行或者忽略日志错误
        new Thread(() -> {
            try {
                crawlerJobHandler.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        return Result.success("爬虫任务已在后台触发执行");
    }
}