package cn.oyzh.easyshell.llm;

/**
 * LLM request parameter defaults and bounds.
 *
 * <p>This class intentionally has no UI or provider dependencies so persisted
 * settings can normalize values without loading model implementations.</p>
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMParameterUtil {

    public static final int DEFAULT_MAX_MESSAGES = 20;

    public static final int MIN_MAX_MESSAGES = 1;

    public static final int MAX_MAX_MESSAGES = 1000;

    public static final int DEFAULT_MAX_OUTPUT_TOKENS = 8192;

    public static final int MIN_MAX_OUTPUT_TOKENS = 256;

    public static final int MAX_MAX_OUTPUT_TOKENS = 131_072;

    public static final int MIN_HISTORY_TOKEN_BUDGET = 1024;

    public static final int DEFAULT_HISTORY_TOKEN_BUDGET = 209_715;

    private LLMParameterUtil() {
    }

    public static int normalizeMaxMessages(int value) {
        return Math.max(MIN_MAX_MESSAGES, Math.min(value, MAX_MAX_MESSAGES));
    }

    public static int normalizeMaxOutputTokens(int value) {
        return Math.max(MIN_MAX_OUTPUT_TOKENS,
                Math.min(value, MAX_MAX_OUTPUT_TOKENS));
    }

    /**
     * Reserves room for system prompts, tool schemas and model output.
     *
     * @param contextWindowTokens model context window
     * @param maxOutputTokens     configured output token limit
     * @return history token budget
     */
    public static int historyTokenBudget(int contextWindowTokens,
                                         int maxOutputTokens) {
        int context = Math.max(MIN_HISTORY_TOKEN_BUDGET, contextWindowTokens);
        int output = normalizeMaxOutputTokens(maxOutputTokens);
        long safetyReserve = Math.max(1024, context / 10L);
        long available = context - output - safetyReserve;
        long eightyPercent = context * 4L / 5L;
        return (int) Math.max(MIN_HISTORY_TOKEN_BUDGET,
                Math.min(available, eightyPercent));
    }
}
