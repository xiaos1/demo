package com.example.aitrends.model;

import java.util.List;

public class FullReport {

    private final int crawledItemCount;
    private final List<ProgramIdea> topIdeas;
    private final FreelanceReport freelanceReport;

    public FullReport(int crawledItemCount, List<ProgramIdea> topIdeas, FreelanceReport freelanceReport) {
        this.crawledItemCount = crawledItemCount;
        this.topIdeas = topIdeas;
        this.freelanceReport = freelanceReport;
    }

    public int getCrawledItemCount() {
        return crawledItemCount;
    }

    public List<ProgramIdea> getTopIdeas() {
        return topIdeas;
    }

    public FreelanceReport getFreelanceReport() {
        return freelanceReport;
    }
}
