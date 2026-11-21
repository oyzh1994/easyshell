package cn.oyzh.easyshell.llm;

import cn.oyzh.easyshell.llm.capability.LLMModelCapability;

import java.io.File;
import java.nio.charset.StandardCharsets;
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
                    result += estimateText(message.getMessage());
                }
            }
        }
        return result;
    }

    static long estimateText(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        long codePoints = text.codePointCount(0, text.length());
        long utf8Bytes = text.getBytes(StandardCharsets.UTF_8).length;
        return Math.max(codePoints, (utf8Bytes + 3L) / 4L);
    }

    private static long estimateFile(File file) {
        if (file == null || !file.isFile()) {
            return 0;
        }
        LLMFileContentType type = LLMFileContentUtil.contentType(file);
        return type == LLMFileContentType.TEXT
                ? Math.max(0, (file.length() + 3L) / 4L)
                : 0;
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
