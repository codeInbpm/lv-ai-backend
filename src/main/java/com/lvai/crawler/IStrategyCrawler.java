package com.lvai.crawler;
import com.lvai.entity.StrategyPost;
import java.util.List;

public interface IStrategyCrawler {
    String getSource();
    List<StrategyPost> crawl(String keyword, int maxPages);
}