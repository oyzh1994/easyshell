package cn.oyzh.easyshell.llm.agent;

/**
 * Agent progress and streaming callbacks.
 *
 * @author oyzh
 * @since 2026-11-20
 */
public interface LLMAgentListener {

    default void onEvent(LLMAgentEvent event) {
    }

    default void onPartialThinking(String text) {
    }

    default void onPartialResponse(String text) {
    }

    /**
     * Receives a snapshot whenever an agent stage starts or changes.
     *
     * @param trace agent trace snapshot
     */
    default void onAgentTrace(LLMAgentTrace trace) {
    }
}
