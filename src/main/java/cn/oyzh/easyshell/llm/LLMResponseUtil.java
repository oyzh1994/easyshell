package cn.oyzh.easyshell.llm;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;

/**
 * Streaming response helpers.
 *
 * @author oyzh
 * @since 2026-11-09
 */
public final class LLMResponseUtil {

    private LLMResponseUtil() {
    }

    /**
     * Returns the complete final text. Some compatible providers only provide it
     * in the final response instead of emitting it as partial text.
     *
     * @param response chat response
     * @return final non-blank text, or {@code null}
     */
    public static String finalText(ChatResponse response) {
        if (response == null || response.aiMessage() == null) {
            return null;
        }
        String text = response.aiMessage().text();
        return text == null || text.isBlank() ? null : text;
    }

    /**
     * Returns the thinking text from the final response.
     *
     * @param response chat response
     * @return final non-blank thinking text, or {@code null}
     */
    public static String finalThinking(ChatResponse response) {
        if (response == null || response.aiMessage() == null) {
            return null;
        }
        String thinking = response.aiMessage().thinking();
        return thinking == null || thinking.isBlank() ? null : thinking;
    }

    /**
     * Merges streamed thinking with the final thinking text without duplicating the final round.
     *
     * @param streamed streamed thinking collected across all tool rounds
     * @param finalThinking thinking from the final response
     * @return merged thinking text
     */
    public static String mergeThinking(String streamed, String finalThinking) {
        if (finalThinking == null || finalThinking.isBlank()) {
            return streamed == null ? "" : streamed;
        }
        if (streamed == null || streamed.isBlank()) {
            return finalThinking;
        }
        return streamed.endsWith(finalThinking) ? streamed : streamed + "\n\n" + finalThinking;
    }

    /**
     * Checks whether the model stopped because the output token limit was reached.
     *
     * @param response chat response
     * @return whether the output was truncated
     */
    public static boolean outputTruncated(ChatResponse response) {
        return response != null && response.finishReason() == FinishReason.LENGTH;
    }
}
