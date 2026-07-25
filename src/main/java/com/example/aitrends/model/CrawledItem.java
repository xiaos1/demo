package com.example.aitrends.model;

public class CrawledItem {

    private final String source;
    private final String title;
    private final String url;
    private final int score;
    private final String snippet;

    public CrawledItem(String source, String title, String url, int score, String snippet) {
        this.source = source;
        this.title = title;
        this.url = url;
        this.score = score;
        this.snippet = snippet;
    }

    public String getSource() {
        return source;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public int getScore() {
        return score;
    }

    public String getSnippet() {
        return snippet;
    }
}
