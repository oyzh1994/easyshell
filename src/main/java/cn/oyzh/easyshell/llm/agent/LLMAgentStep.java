package cn.oyzh.easyshell.llm.agent;

/**
 * One planned agent step.
 *
 * @author oyzh
 * @since 2026-11-20
 */
public record LLMAgentStep(int index,
                           LLMAgentType type,
                           String objective,
                           String reason) {
}
