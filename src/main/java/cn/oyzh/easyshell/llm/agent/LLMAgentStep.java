package cn.oyzh.easyshell.llm.agent;

import java.util.List;

/**
 * One planned agent step.
 *
 * @author oyzh
 * @since 2026-11-20
 */
public record LLMAgentStep(int index,
                           String id,
                           LLMAgentType type,
                           String objective,
                           String reason,
                           List<String> dependsOn) {

    public LLMAgentStep {
        id = id == null || id.isBlank() ? "step-" + (index + 1) : id.trim();
        dependsOn = dependsOn == null ? List.of() : List.copyOf(dependsOn);
    }

    public LLMAgentStep(int index,
                        LLMAgentType type,
                        String objective,
                        String reason) {
        this(index, "step-" + (index + 1), type, objective, reason, List.of());
    }
}
