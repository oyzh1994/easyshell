package cn.oyzh.easyshell.llm;

import dev.langchain4j.model.output.TokenUsage;

import java.util.Locale;

/**
 * Immutable token usage statistics for one LLM session.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public record LLMTokenUsageStats(
        long lastInputTokens,
        long lastOutputTokens,
        long lastTotalTokens,
        long sessionInputTokens,
        long sessionOutputTokens,
        long sessionTotalTokens,
        boolean cumulativeUsageComplete,
        long lastDurationMillis) {

    public static final long UNKNOWN = -1;

    public static final LLMTokenUsageStats EMPTY = new LLMTokenUsageStats(
            UNKNOWN, UNKNOWN, UNKNOWN,
            UNKNOWN, UNKNOWN, UNKNOWN,
            true, UNKNOWN);

    /**
     * Returns the latest context occupancy. The input count represents the
     * complete prompt sent by the provider for the last model call.
     *
     * @return token count, or {@link #UNKNOWN}
     */
    public long contextTokens() {
        return this.lastInputTokens >= 0 ? this.lastInputTokens : UNKNOWN;
    }

    public boolean hasLastUsage() {
        return this.lastInputTokens >= 0
                || this.lastOutputTokens >= 0
                || this.lastTotalTokens >= 0;
    }

    public boolean hasSessionUsage() {
        return this.sessionInputTokens > 0
                || this.sessionOutputTokens > 0
                || this.sessionTotalTokens > 0;
    }

    /**
     * Formats a token count compactly for the toolbar and tooltip.
     *
     * @param value token count
     * @return formatted count
     */
    public static String formatTokens(long value) {
        if (value < 0) {
            return "?";
        }
        if (value >= 1_000_000L) {
            return String.format(Locale.ROOT, "%.1fM", value / 1_000_000.0);
        }
        if (value >= 1_000L) {
            return String.format(Locale.ROOT, "%.1fK", value / 1_000.0);
        }
        return Long.toString(value);
    }

    /**
     * Adds one completed raw model response to the statistics.
     *
     * @param usage    provider token usage
     * @param duration request duration in milliseconds
     * @return updated statistics
     */
    public LLMTokenUsageStats record(TokenUsage usage, long duration) {
        long input = tokenCount(usage == null ? null : usage.inputTokenCount());
        long output = tokenCount(usage == null ? null : usage.outputTokenCount());
        long total = tokenCount(usage == null ? null : usage.totalTokenCount());
        if (total < 0) {
            total = sumKnown(input, output);
        }
        boolean usageComplete = input >= 0 && output >= 0 && total >= 0;
        if (!hasAny(input, output, total)) {
            return new LLMTokenUsageStats(
                    UNKNOWN, UNKNOWN, UNKNOWN,
                    this.sessionInputTokens, this.sessionOutputTokens,
                    this.sessionTotalTokens, false, duration);
        }
        return new LLMTokenUsageStats(
                input, output, total,
                addKnown(this.sessionInputTokens, input),
                addKnown(this.sessionOutputTokens, output),
                addKnown(this.sessionTotalTokens, total),
                this.cumulativeUsageComplete && usageComplete,
                duration);
    }

    /**
     * Applies the request duration without changing token counters.
     *
     * @param duration request duration in milliseconds
     * @return updated statistics
     */
    public LLMTokenUsageStats withDuration(long duration) {
        return new LLMTokenUsageStats(
                this.lastInputTokens, this.lastOutputTokens, this.lastTotalTokens,
                this.sessionInputTokens, this.sessionOutputTokens,
                this.sessionTotalTokens, this.cumulativeUsageComplete,
                duration);
    }

    /**
     * Marks the last usage as unavailable while retaining known session subtotals.
     *
     * @param duration request duration in milliseconds
     * @return updated statistics
     */
    public LLMTokenUsageStats withoutLastUsage(long duration) {
        return new LLMTokenUsageStats(
                UNKNOWN, UNKNOWN, UNKNOWN,
                this.sessionInputTokens, this.sessionOutputTokens,
                this.sessionTotalTokens, false, duration);
    }

    private static long tokenCount(Integer value) {
        return value == null || value < 0 ? UNKNOWN : value;
    }

    private static boolean hasAny(long... values) {
        for (long value : values) {
            if (value >= 0) {
                return true;
            }
        }
        return false;
    }

    private static long sumKnown(long first, long second) {
        if (first < 0 && second < 0) {
            return UNKNOWN;
        }
        return Math.max(0, first) + Math.max(0, second);
    }

    private static long addKnown(long total, long value) {
        if (total < 0 || value < 0) {
            return total < 0 && value >= 0 ? value : total;
        }
        return total + value;
    }
}
