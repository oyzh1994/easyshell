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

    private LLMParameterUtil() {
    }

    public static int normalizeMaxMessages(int value) {
        return Math.max(MIN_MAX_MESSAGES, Math.min(value, MAX_MAX_MESSAGES));
    }

    public static int normalizeMaxOutputTokens(int value) {
        return Math.max(MIN_MAX_OUTPUT_TOKENS,
                Math.min(value, MAX_MAX_OUTPUT_TOKENS));
    }
}
