package cn.oyzh.easyshell.llm;

import cn.oyzh.easyshell.llm.capability.LLMModelCapability;

import java.io.File;
import java.util.List;

/**
 * Preflight estimate for context-window capacity.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMContextBudgetUtil {

    private LLMContextBudgetUtil() {
    }

    /**
     * Estimates whether a new request can fit in the configured model context.
     *
     * @param stats         latest token statistics
     * @param messages      current session messages
     * @param prompt        new user prompt
     * @param file          attachment
     * @param maxOutput     configured output budget
     * @param capability    model capability
     * @return budget result
     */
    public static Result check(LLMTokenUsageStats stats,
                               List<LLMMessage> messages,
                               String prompt,
                               File file,
                               int maxOutput,
                               LLMModelCapability capability) {
        long limit = capability.getContextWindowTokens();
        long used = stats == null ? LLMTokenUsageStats.UNKNOWN : stats.contextTokens();
        if (used < 0) {
            used = estimateMessages(messages);
        }
        long estimated = used
                + estimateText(prompt)
                + estimateFile(file)
                + Math.max(0, maxOutput);
        return new Result(estimated <= limit, estimated, limit);
    }

    static long estimateMessages(List<LLMMessage> messages) {
        long result = 0;
        if (messages != null) {
            for (LLMMessage message : messages) {
                if (message != null && !message.isError()) {
                    result += LLMTokenEstimator.estimate(message);
                }
            }
        }
        return result;
    }

    static long estimateText(String text) {
        return LLMTokenEstimator.estimateText(text);
    }

    private static long estimateFile(File file) {
        return LLMTokenEstimator.estimateFile(file);
    }

    /**
     * Context budget result.
     *
     * @param allowed         whether request fits
     * @param estimatedTokens estimated request tokens
     * @param limitTokens     configured context tokens
     */
    public record Result(boolean allowed,
                         long estimatedTokens,
                         long limitTokens) {
    }
}
