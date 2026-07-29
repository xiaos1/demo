package com.example.aitrends.controller;

import com.example.aitrends.model.CrawledItem;
import com.example.aitrends.model.FreelanceReport;
import com.example.aitrends.model.FullReport;
import com.example.aitrends.model.ProgramIdea;
import com.example.aitrends.service.CrawlerService;
import com.example.aitrends.service.FreelanceAdvisorService;
import com.example.aitrends.service.IdeaAnalyzerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AiTrendsController {

    private final CrawlerService crawlerService;
    private final IdeaAnalyzerService ideaAnalyzerService;
    private final FreelanceAdvisorService freelanceAdvisorService;

    public AiTrendsController(CrawlerService crawlerService, IdeaAnalyzerService ideaAnalyzerService,
                              FreelanceAdvisorService freelanceAdvisorService) {
        this.crawlerService = crawlerService;
        this.ideaAnalyzerService = ideaAnalyzerService;
        this.freelanceAdvisorService = freelanceAdvisorService;
    }

    /**
     * Feature 1: crawl public internet sources for signal about hot AI-beginner programs.
     */
    @GetMapping("/api/crawl")
    public List<CrawledItem> crawl() {
        return crawlerService.crawlAll();
    }

    /**
     * Feature 2: print + return the most popular AI program ideas for beginners.
     */
    @GetMapping("/api/ideas/popular")
    public List<ProgramIdea> popularIdeas(@RequestParam(defaultValue = "10") int limit) {
        List<CrawledItem> items = crawlerService.crawlAll();
        return ideaAnalyzerService.rankIdeas(items, limit);
    }

    /**
     * Feature 3: freelancer suggestions built from the current top ideas.
     */
    @GetMapping("/api/freelance/suggestions")
    public FreelanceReport freelanceSuggestions(@RequestParam(defaultValue = "5") int limit) {
        List<CrawledItem> items = crawlerService.crawlAll();
        List<ProgramIdea> topIdeas = ideaAnalyzerService.rankIdeas(items, limit);
        return freelanceAdvisorService.buildReport(topIdeas);
    }

    /**
     * Combined view: crawl -> rank ideas -> freelance suggestions, in one call.
     */
    @GetMapping("/api/report")
    public FullReport fullReport(@RequestParam(defaultValue = "10") int ideaLimit,
                                 @RequestParam(defaultValue = "5") int freelanceLimit) {
        List<CrawledItem> items = crawlerService.crawlAll();
        List<ProgramIdea> topIdeas = ideaAnalyzerService.rankIdeas(items, ideaLimit);
        FreelanceReport freelanceReport = freelanceAdvisorService.buildReport(
                topIdeas.size() > freelanceLimit ? topIdeas.subList(0, freelanceLimit) : topIdeas);
        return new FullReport(items.size(), topIdeas, freelanceReport);
    }
}
