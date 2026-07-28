package com.example.aitrends.model;

import java.util.List;

public class FreelanceSuggestion {

    private final String ideaName;
    private final List<String> platforms;
    private final String pricingRange;
    private final String clientPitch;
    private final List<String> portfolioTips;

    public FreelanceSuggestion(String ideaName, List<String> platforms, String pricingRange,
                                String clientPitch, List<String> portfolioTips) {
        this.ideaName = ideaName;
        this.platforms = platforms;
        this.pricingRange = pricingRange;
        this.clientPitch = clientPitch;
        this.portfolioTips = portfolioTips;
    }

    public String getIdeaName() {
        return ideaName;
    }

    public String getPricingRange() {
        return pricingRange;
    }

    public String getClientPitch() {
        return clientPitch;
    }

    public List<String> getPortfolioTips() {
        return portfolioTips;
    }
}
