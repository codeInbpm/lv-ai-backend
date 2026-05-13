package com.lvai.crawler;
import com.lvai.entity.StrategyPost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class XiaohongshuCrawlerImpl implements IStrategyCrawler {
    @Override
    public String getSource() {
        return "xiaohongshu";
    }

    @Override
    public List<StrategyPost> crawl(String keyword, int maxPages) {
        log.info("开始抓取小红书, 关键字: {}, 页数: {}", keyword, maxPages);
        List<StrategyPost> result = new ArrayList<>();
        // 小红书防爬极其严格，通常需要无头浏览器或Cookie。这里演示直接返回 Mock 结构
        StrategyPost post = new StrategyPost();
        post.setSource(getSource());
        post.setExternalId("xhs_mock_" + System.currentTimeMillis());
        post.setTitle("小红书爆款：" + keyword + "怎么玩？");
        post.setContent(keyword + "旅游必须打卡的几个地方，快来抄作业！...");
        post.setCoverUrl("https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=500");
        post.setDestination(keyword);
        post.setLikeCount(5200);
        post.setViewCount(12000);
        post.setStatus(0);
        result.add(post);
        return result;
    }
}