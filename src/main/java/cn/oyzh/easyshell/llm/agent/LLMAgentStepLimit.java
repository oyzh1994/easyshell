package cn.oyzh.easyshell.llm.agent;

import java.util.List;

/**
 * Supported maximum plan-step counts.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMAgentStepLimit {

    public static final int DEFAULT = 10;

    public static final List<Integer> OPTIONS = List.of(6, 10, 15, 20, 30);

    private LLMAgentStepLimit() {
    }

    public static int normalize(int value) {
        return OPTIONS.contains(value) ? value : DEFAULT;
    }
}
