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
        return formatTokens(tokens);
    }

    /**
     * Formats token counts using binary units so a 256K context displays as 256K.
     *
     * @param tokens token count
     * @return compact binary-unit label
     */
    public static String formatTokens(long tokens) {
        if (tokens < 0) {
            return "?";
        }
        if (tokens >= 1024L * 1024) {
            return compact(tokens / (1024.0 * 1024.0)) + "M";
        }
        if (tokens >= 1024L) {
            return compact(tokens / 1024.0) + "K";
        }
        return Long.toString(tokens);
    }

    private static String compact(double value) {
        String formatted = String.format(Locale.ROOT, "%.1f", value);
        return formatted.endsWith(".0")
                ? formatted.substring(0, formatted.length() - 2)
                : formatted;
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
