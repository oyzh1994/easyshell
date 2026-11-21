package cn.oyzh.easyshell.llm;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.VideoContent;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Conservative token estimator used when a provider does not expose tokenization.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMTokenEstimator {

    private static final long MEDIA_TOKEN_ESTIMATE = 1024;

    private LLMTokenEstimator() {
    }

    public static long estimate(ChatMessage message) {
        if (message == null) {
            return 0;
        }
        if (message instanceof SystemMessage systemMessage) {
            return estimateText(systemMessage.text());
        }
        if (message instanceof UserMessage userMessage) {
            long result = 0;
            for (Content content : userMessage.contents()) {
                result += estimate(content);
            }
            return result;
        }
        if (message instanceof AiMessage aiMessage) {
            long result = estimateText(aiMessage.text())
                    + estimateText(aiMessage.thinking());
            for (ToolExecutionRequest request : aiMessage.toolExecutionRequests()) {
                result += estimateText(request.name())
                        + estimateText(request.arguments());
            }
            return result;
        }
        if (message instanceof ToolExecutionResultMessage toolResult) {
            long result = 0;
            for (Content content : toolResult.contents()) {
                result += estimate(content);
            }
            return result;
        }
        return estimateText(message.toString());
    }

    public static long estimate(LLMMessage message) {
        if (message == null || message.isError()) {
            return 0;
        }
        return estimateText(message.getMessage()) + estimateFile(message.getFile());
    }

    public static long estimateText(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        long codePoints = text.codePointCount(0, text.length());
        long utf8Bytes = text.getBytes(StandardCharsets.UTF_8).length;
        return Math.max(codePoints, (utf8Bytes + 3L) / 4L);
    }

    static long estimateFile(File file) {
        if (file == null || !file.isFile()) {
            return 0;
        }
        return LLMFileContentUtil.contentType(file) == LLMFileContentType.TEXT
                ? Math.max(0, (file.length() + 3L) / 4L)
                : 0;
    }

    private static long estimate(Content content) {
        if (content instanceof TextContent textContent) {
            return estimateText(textContent.text());
        }
        if (content instanceof ImageContent
                || content instanceof PdfFileContent
                || content instanceof AudioContent
                || content instanceof VideoContent) {
            return MEDIA_TOKEN_ESTIMATE;
        }
        return estimateText(content == null ? null : content.toString());
    }
}
