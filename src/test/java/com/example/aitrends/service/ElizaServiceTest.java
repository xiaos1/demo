package com.example.aitrends.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElizaServiceTest {

    private final ElizaService eliza = new ElizaService();

    @Test
    void reflectsPronounsWhenEchoingBackAStatement() {
        String reply = eliza.respond("s1", "I am feeling sad today");
        assertThat(reply).contains("feeling sad today");
    }

    @Test
    void respondsToIWantWithAQuestionAboutTheGoal() {
        String reply = eliza.respond("s2", "I want a new job");
        assertThat(reply).containsIgnoringCase("a new job");
    }

    @Test
    void recallsFamilyKeyword() {
        String reply = eliza.respond("s3", "My mother never listens to me");
        assertThat(reply.toLowerCase()).contains("mother");
    }

    @Test
    void fallsBackToGenericPromptWhenNoKeywordMatches() {
        String reply = eliza.respond("s4", "xyzzy plugh");
        assertThat(reply).isNotBlank();
    }

    @Test
    void recognizesExitWords() {
        String reply = eliza.respond("s5", "bye");
        assertThat(reply.toLowerCase()).containsAnyOf("goodbye", "bye");
    }

    @Test
    void doesNotTreatQuitAsExitWhenUsedMidSentence() {
        String reply = eliza.respond("s7", "My mother thinks I should quit");
        assertThat(reply.toLowerCase()).doesNotContain("goodbye");
    }

    @Test
    void sameSessionCyclesThroughFallbacksInsteadOfRepeating() {
        String first = eliza.respond("s6", "xyzzy");
        String second = eliza.respond("s6", "plugh");
        assertThat(first).isNotEqualTo(second);
    }
}
