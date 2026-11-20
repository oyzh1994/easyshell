package cn.oyzh.easyshell.llm.agent;

/**
 * Controls how the orchestrator reacts when a worker fails.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public enum LLMAgentFailurePolicy {

    /**
     * Continue independent work and let the final reporter describe failures.
     */
    CONTINUE,

    /**
     * Stop scheduling and terminate the request.
     */
    FAIL_FAST,

    /**
     * Retry a failed worker once, then continue with the failure if it still fails.
     */
    RETRY;

    public static LLMAgentFailurePolicy fromOrdinal(Integer ordinal) {
        LLMAgentFailurePolicy[] policies = values();
        return ordinal == null || ordinal < 0 || ordinal >= policies.length
                ? CONTINUE
                : policies[ordinal];
    }
}
