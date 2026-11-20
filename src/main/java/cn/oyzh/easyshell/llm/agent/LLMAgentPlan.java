package cn.oyzh.easyshell.llm.agent;

import java.util.List;

/**
 * Supervisor plan.
 *
 * @author oyzh
 * @since 2026-11-20
 */
public record LLMAgentPlan(String objective, List<LLMAgentStep> steps) {

    public LLMAgentPlan {
        steps = List.copyOf(steps);
    }

    public boolean composite() {
        return this.steps.size() > 1;
    }
}
