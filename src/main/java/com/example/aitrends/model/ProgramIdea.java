package com.example.aitrends.model;

import java.util.List;

public class ProgramIdea {

    private int rank;
    private final String name;
    private final String description;
    private final String difficulty;
    private final List<String> techStack;
    private final int popularityScore;
    private final List<String> mentionedOn;

    public ProgramIdea(String name, String description, String difficulty,
                        List<String> techStack, int popularityScore, List<String> mentionedOn) {
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.techStack = techStack;
        this.popularityScore = popularityScore;
        this.mentionedOn = mentionedOn;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public List<String> getTechStack() {
        return techStack;
    }

    public int getPopularityScore() {
        return popularityScore;
    }

    public List<String> getMentionedOn() {
        return mentionedOn;
    }
}
