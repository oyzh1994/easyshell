package cn.oyzh.easyshell.llm.agent;

/**
 * Controls whether one request uses the composite agent pipeline.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public enum LLMAgentMode {

    /**
     * Planner, scoped workers, and final aggregation.
     */
    MULTI,

    /**
     * One direct agent without planner or final aggregation.
     */
    SINGLE;

    public static LLMAgentMode fromOrdinal(Integer ordinal) {
        LLMAgentMode[] modes = values();
        return ordinal == null || ordinal < 0 || ordinal >= modes.length
                ? MULTI
                : modes[ordinal];
    }
}
