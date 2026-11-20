package cn.oyzh.easyshell.llm.agent;

/**
 * Observable agent lifecycle event.
 *
 * @author oyzh
 * @since 2026-11-20
 */
public record LLMAgentEvent(LLMAgentEventType type,
                            int stepIndex,
                            int totalSteps,
                            String objective) {


}
