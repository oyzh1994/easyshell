package cn.oyzh.easyshell.llm.agent;

import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * Result produced by one agent worker.
 *
 * @author oyzh
 * @since 2026-11-20
 */
public record LLMAgentStepResult(LLMAgentStep step,
                                 String output,
                                 String error,
                                 ChatResponse response) {

    public boolean failed() {
        return this.error != null || this.output == null || this.output.isBlank();
    }
}
