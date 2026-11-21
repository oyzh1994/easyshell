package cn.oyzh.easyshell.llm;

import java.util.List;
import java.util.Locale;

/**
 * Supported model context-window values.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMModelContextWindowUtil {

    public static final int DEFAULT_TOKENS = 256 * 1024;

    public static final List<Integer> OPTIONS = List.of(
            64 * 1024,
            128 * 1024,
            DEFAULT_TOKENS,
            384 * 1024,
            512 * 1024,
            1024 * 1024);

    private LLMModelContextWindowUtil() {
    }

    /**
     * Normalizes a configured value to one of the supported values.
     *
     * @param tokens configured token count
     * @return supported token count
     */
    public static int normalize(int tokens) {
        return OPTIONS.contains(tokens) ? tokens : DEFAULT_TOKENS;
    }

    /**
     * Formats a context-window option for menus and tooltips.
     *
     * @param tokens token count
     * @return compact label
     */
    public static String label(int tokens) {
        if (tokens >= 1024 * 1024) {
            return String.format(Locale.ROOT, "%.0fM",
                    tokens / (1024.0 * 1024.0));
        }
        return String.format(Locale.ROOT, "%dK", tokens / 1024);
    }

    /**
     * Context capacity and whether the value is an estimate.
     *
     * @param tokens    context token capacity
     * @param estimated whether the value is heuristic
     */
    public record ContextWindow(long tokens, boolean estimated) {

        public static final ContextWindow UNKNOWN = new ContextWindow(-1, false);

        public boolean known() {
            return this.tokens > 0;
        }
    }
}
