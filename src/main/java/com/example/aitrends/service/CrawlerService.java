package com.example.aitrends.service;

import com.example.aitrends.model.CrawledItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Pulls signal about trending/beginner-friendly AI projects from a handful of
 * public, key-less internet sources (GitHub, Hacker News, Dev.to, Reddit).
 * Each source is fetched in parallel and fails independently so a single
 * outage or rate limit never breaks the aggregate result.
 */
@Service
public class CrawlerService {

    private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);
    private static final String USER_AGENT = "ai-beginner-programs-crawler/1.0";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ExecutorService crawlerExecutor;

    public CrawlerService(RestTemplate restTemplate, ObjectMapper objectMapper, ExecutorService crawlerExecutor) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.crawlerExecutor = crawlerExecutor;
    }

    public List<CrawledItem> crawlAll() {
        CompletableFuture<List<CrawledItem>> github = CompletableFuture.supplyAsync(
                () -> safeCrawl("GitHub", this::crawlGitHub), crawlerExecutor);
        CompletableFuture<List<CrawledItem>> hackerNews = CompletableFuture.supplyAsync(
                () -> safeCrawl("HackerNews", this::crawlHackerNews), crawlerExecutor);
        CompletableFuture<List<CrawledItem>> devTo = CompletableFuture.supplyAsync(
                () -> safeCrawl("Dev.to", this::crawlDevTo), crawlerExecutor);
        CompletableFuture<List<CrawledItem>> reddit = CompletableFuture.supplyAsync(
                () -> safeCrawl("Reddit", this::crawlReddit), crawlerExecutor);

        List<CrawledItem> items = new ArrayList<>();
        for (CompletableFuture<List<CrawledItem>> future : List.of(github, hackerNews, devTo, reddit)) {
            items.addAll(future.join());
        }

        log.info("Crawl finished: {} items collected from 4 sources", items.size());
        return items;
    }

    private List<CrawledItem> safeCrawl(String sourceName, Supplier<List<CrawledItem>> crawlFn) {
        try {
            List<CrawledItem> result = crawlFn.get();
            log.info("Source [{}] returned {} items", sourceName, result.size());
            return result;
        } catch (Exception e) {
            log.warn("Source [{}] crawl failed, skipping ({}: {})", sourceName,
                    e.getClass().getSimpleName(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<CrawledItem> crawlGitHub() {
        String url = "https://api.github.com/search/repositories"
                + "?q=" + "artificial-intelligence+beginner+project+tutorial"
                + "&sort=stars&order=desc&per_page=20";
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", USER_AGENT);
        headers.set("Accept", "application/vnd.github+json");

        JsonNode root = fetchJson(url, headers);
        List<CrawledItem> items = new ArrayList<>();
        if (root == null) {
            return items;
        }
        for (JsonNode repo : root.path("items")) {
            items.add(new CrawledItem(
                    "GitHub",
                    repo.path("full_name").asText(""),
                    repo.path("html_url").asText(""),
                    repo.path("stargazers_count").asInt(0),
                    repo.path("description").asText("")));
        }
        return items;
    }

    private List<CrawledItem> crawlHackerNews() {
        String url = "https://hn.algolia.com/api/v1/search"
                + "?query=" + "AI%20project%20beginner"
                + "&tags=story&hitsPerPage=20";
        JsonNode root = fetchJson(url, new HttpHeaders());
        List<CrawledItem> items = new ArrayList<>();
        if (root == null) {
            return items;
        }
        for (JsonNode hit : root.path("hits")) {
            items.add(new CrawledItem(
                    "HackerNews",
                    hit.path("title").asText(""),
                    hit.path("url").asText(""),
                    hit.path("points").asInt(0),
                    ""));
        }
        return items;
    }

    private List<CrawledItem> crawlDevTo() {
        String url = "https://dev.to/api/articles?tag=ai&top=7&per_page=20";
        JsonNode root = fetchJson(url, new HttpHeaders());
        List<CrawledItem> items = new ArrayList<>();
        if (root == null) {
            return items;
        }
        for (JsonNode article : root) {
            items.add(new CrawledItem(
                    "Dev.to",
                    article.path("title").asText(""),
                    article.path("url").asText(""),
                    article.path("positive_reactions_count").asInt(0),
                    article.path("description").asText("")));
        }
        return items;
    }

    private List<CrawledItem> crawlReddit() {
        String url = "https://www.reddit.com/r/learnmachinelearning/top.json?limit=20&t=month";
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", USER_AGENT);

        JsonNode root = fetchJson(url, headers);
        List<CrawledItem> items = new ArrayList<>();
        if (root == null) {
            return items;
        }
        for (JsonNode child : root.path("data").path("children")) {
            JsonNode data = child.path("data");
            items.add(new CrawledItem(
                    "Reddit",
                    data.path("title").asText(""),
                    "https://www.reddit.com" + data.path("permalink").asText(""),
                    data.path("ups").asInt(0),
                    ""));
        }
        return items;
    }

    private JsonNode fetchJson(String url, HttpHeaders headers) {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String body = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            log.warn("Failed to parse JSON from {}: {}", url, e.getMessage());
            return null;
        }
    }
}
