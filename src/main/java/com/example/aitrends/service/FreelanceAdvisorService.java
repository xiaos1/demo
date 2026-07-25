package com.example.aitrends.service;

import com.example.aitrends.model.FreelanceReport;
import com.example.aitrends.model.FreelanceSuggestion;
import com.example.aitrends.model.ProgramIdea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Turns ranked AI project ideas into concrete freelancing suggestions:
 * which platforms to list the gig on, how to price it, and how to pitch it,
 * based on the idea's difficulty tier.
 */
@Service
public class FreelanceAdvisorService {

    private static final Logger log = LoggerFactory.getLogger(FreelanceAdvisorService.class);

    private static final List<String> PLATFORMS = List.of(
            "Upwork", "Fiverr", "Toptal", "Freelancer.com", "Contra", "LinkedIn Services");

    private static final List<String> GENERAL_TIPS = List.of(
            "Build 3-5 polished, deployed portfolio projects with clear READMEs, demos and short demo videos.",
            "Package beginner projects as fixed-scope, fixed-price gigs (e.g. \"custom chatbot in 5 days\") instead of hourly work.",
            "Pick a niche (e.g. AI chatbots for e-commerce, resume screeners for recruiters) so your profile stands out from generic 'AI developer' listings.",
            "Start on Upwork/Fiverr to collect reviews, then graduate to Toptal or direct/referral clients for higher rates.",
            "Publish build-in-public content (blog posts, LinkedIn/X threads, short demo videos) to attract inbound leads instead of only bidding on jobs.",
            "Offer a free 15-30 min discovery call to scope the client's problem before quoting a price - it builds trust and reduces scope creep.",
            "Use ready-made APIs (OpenAI, HuggingFace, Whisper) to ship fast; you don't need to train models from scratch to deliver client value.",
            "Always clarify data privacy/ownership and hosting costs up front in the contract - clients often forget these until after delivery."
    );

    public FreelanceReport buildReport(List<ProgramIdea> topIdeas) {
        List<FreelanceSuggestion> suggestions = topIdeas.stream()
                .map(this::toSuggestion)
                .collect(Collectors.toList());

        FreelanceReport report = new FreelanceReport(suggestions, GENERAL_TIPS, PLATFORMS);
        printReport(report);
        return report;
    }

    private FreelanceSuggestion toSuggestion(ProgramIdea idea) {
        String pricingRange = "Beginner".equalsIgnoreCase(idea.getDifficulty())
                ? "$150 - $600 per fixed-scope project (or $25-$50/hr)"
                : "$500 - $2,500 per fixed-scope project (or $50-$100/hr)";

        String pitch = String.format(Locale.ROOT,
                "\"I build custom %s solutions for small businesses - delivered in days, using proven APIs, "
                        + "with a working demo before you pay in full.\"",
                idea.getName().toLowerCase(Locale.ROOT));

        List<String> portfolioTips = List.of(
                "Ship one polished, deployed demo of a " + idea.getName() + " with a short (<2 min) walkthrough video.",
                "Write a short case-study post explaining the problem, your approach, and the measurable result.",
                "List the exact tech stack (" + String.join(", ", idea.getTechStack()) + ") on your profile to match client searches.");

        return new FreelanceSuggestion(idea.getName(), PLATFORMS, pricingRange, pitch, portfolioTips);
    }

    private void printReport(FreelanceReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator())
          .append("=========== Freelancer Suggestions Based on Trending AI Ideas ===========")
          .append(System.lineSeparator());
        for (FreelanceSuggestion s : report.getIdeaSuggestions()) {
            sb.append(String.format(Locale.ROOT, "- %-40s pricing=%s%n", s.getIdeaName(), s.getPricingRange()));
        }
        sb.append("Recommended platforms: ").append(report.getRecommendedPlatforms()).append(System.lineSeparator());
        sb.append("==========================================================================");
        log.info(sb.toString());
    }
}
