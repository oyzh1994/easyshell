package cn.oyzh.easyshell.llm;

/**
 * Formats task durations for the LLM toolbar.
 *
 * @author oyzh
 * @since 2026-11-09
 */
public final class LLMDurationUtil {

    private LLMDurationUtil() {
    }

    /**
     * Formats elapsed time as {@code mm:ss} or {@code HH:mm:ss}.
     *
     * @param millis elapsed milliseconds
     * @return formatted duration
     */
    public static String format(long millis) {
        long totalSeconds = Math.max(0, millis) / 1000;
        long hours = totalSeconds / 3600;
        long minutes = totalSeconds % 3600 / 60;
        long seconds = totalSeconds % 60;
        return hours > 0
                ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
    }
}
