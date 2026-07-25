package com.example.aitrends.model;

import java.util.List;

public class FreelanceReport {

    private final List<FreelanceSuggestion> ideaSuggestions;
    private final List<String> generalTips;
    private final List<String> recommendedPlatforms;

    public FreelanceReport(List<FreelanceSuggestion> ideaSuggestions, List<String> generalTips,
                            List<String> recommendedPlatforms) {
        this.ideaSuggestions = ideaSuggestions;
        this.generalTips = generalTips;
        this.recommendedPlatforms = recommendedPlatforms;
    }

    public List<FreelanceSuggestion> getIdeaSuggestions() {
        return ideaSuggestions;
    }

    public List<String> getGeneralTips() {
        return generalTips;
    }

    public List<String> getRecommendedPlatforms() {
        return recommendedPlatforms;
    }
}
