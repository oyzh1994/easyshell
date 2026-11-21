package cn.oyzh.easyshell.llm;

import dev.langchain4j.model.output.TokenUsage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LLMTokenUsageStatsTest {

    @Test
    public void accumulatesKnownUsageAndTracksLatestContext() {
        LLMTokenUsageStats stats = LLMTokenUsageStats.EMPTY
                .record(new TokenUsage(100, 20, 120), LLMTokenUsageStats.UNKNOWN)
                .record(new TokenUsage(150, 30, 180), LLMTokenUsageStats.UNKNOWN)
                .withDuration(2_500);

        assertTrue(stats.hasLastUsage());
        assertTrue(stats.hasSessionUsage());
        assertTrue(stats.cumulativeUsageComplete());
        assertEquals(150, stats.contextTokens());
        assertEquals(150, stats.lastInputTokens());
        assertEquals(30, stats.lastOutputTokens());
        assertEquals(180, stats.lastTotalTokens());
        assertEquals(250, stats.sessionInputTokens());
        assertEquals(50, stats.sessionOutputTokens());
        assertEquals(300, stats.sessionTotalTokens());
        assertEquals(2_500, stats.lastDurationMillis());
    }

    @Test
    public void missingProviderUsageNeverFabricatesAContextRatio() {
        LLMTokenUsageStats stats = LLMTokenUsageStats.EMPTY
                .record(new TokenUsage(100, 20), LLMTokenUsageStats.UNKNOWN)
                .record(null, 800);

        assertFalse(stats.hasLastUsage());
        assertFalse(stats.cumulativeUsageComplete());
        assertEquals(LLMTokenUsageStats.UNKNOWN, stats.contextTokens());
        assertEquals(100, stats.sessionInputTokens());
        assertEquals(20, stats.sessionOutputTokens());
        assertEquals(120, stats.sessionTotalTokens());
        assertEquals(800, stats.lastDurationMillis());
    }

    @Test
    public void formatsCompactTokenCounts() {
        assertEquals("?", LLMTokenUsageStats.formatTokens(-1));
        assertEquals("999", LLMTokenUsageStats.formatTokens(999));
        assertEquals("12.5K", LLMTokenUsageStats.formatTokens(12_500));
        assertEquals("1.2M", LLMTokenUsageStats.formatTokens(1_200_000));
    }
}
