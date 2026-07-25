package com.example.aitrends;

import com.example.aitrends.model.CrawledItem;
import com.example.aitrends.model.ProgramIdea;
import com.example.aitrends.service.CrawlerService;
import com.example.aitrends.service.FreelanceAdvisorService;
import com.example.aitrends.service.IdeaAnalyzerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * On boot, kicks off one crawl + ranking + freelance-advice pass in the
 * background and prints it to the console, so the three features are visible
 * immediately without needing to call the REST endpoints first.
 */
@Component
public class StartupReportRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupReportRunner.class);

    private final CrawlerService crawlerService;
    private final IdeaAnalyzerService ideaAnalyzerService;
    private final FreelanceAdvisorService freelanceAdvisorService;

    public StartupReportRunner(CrawlerService crawlerService, IdeaAnalyzerService ideaAnalyzerService,
                                FreelanceAdvisorService freelanceAdvisorService) {
        this.crawlerService = crawlerService;
        this.ideaAnalyzerService = ideaAnalyzerService;
        this.freelanceAdvisorService = freelanceAdvisorService;
    }

    @Override
    public void run(ApplicationArguments args) {
        CompletableFuture.runAsync(() -> {
            log.info("Running startup crawl for hot AI-beginner programs...");
            List<CrawledItem> items = crawlerService.crawlAll();
            List<ProgramIdea> topIdeas = ideaAnalyzerService.rankIdeas(items, 10);
            freelanceAdvisorService.buildReport(topIdeas.size() > 5 ? topIdeas.subList(0, 5) : topIdeas);
        }, Executors.newSingleThreadExecutor()).exceptionally(ex -> {
            log.warn("Startup report failed, app will still serve /api/* on demand: {}", ex.getMessage());
            return null;
        });
    }
}
