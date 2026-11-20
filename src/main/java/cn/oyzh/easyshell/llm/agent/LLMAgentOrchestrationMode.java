package cn.oyzh.easyshell.llm.agent;

import cn.oyzh.i18n.I18nResourceBundle;

/**
 * Execution topology used by the multi-agent pipeline.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public enum LLMAgentOrchestrationMode {

    /**
     * Current supervisor-worker pipeline with strictly ordered steps.
     */
    SEQUENTIAL,

    /**
     * Execute independent steps concurrently, subject to the concurrency limit.
     */
    PARALLEL,

    /**
     * Execute a dependency graph, scheduling all currently ready steps.
     */
    DAG,

    /**
     * Ask the supervisor for one next step after every completed step.
     */
    DYNAMIC_SUPERVISOR;

    public static LLMAgentOrchestrationMode fromOrdinal(Integer ordinal) {
        LLMAgentOrchestrationMode[] modes = values();
        return ordinal == null || ordinal < 0 || ordinal >= modes.length
                ? SEQUENTIAL
                : modes[ordinal];
    }

    public String getDisplayName() {
        return switch (this) {
            case SEQUENTIAL -> I18nResourceBundle.i18nString(
                    "shell.llm.agent.orchestration.sequential");
            case PARALLEL -> I18nResourceBundle.i18nString(
                    "shell.llm.agent.orchestration.parallel");
            case DAG -> I18nResourceBundle.i18nString(
                    "shell.llm.agent.orchestration.dag");
            case DYNAMIC_SUPERVISOR -> I18nResourceBundle.i18nString(
                    "shell.llm.agent.orchestration.dynamic");
        };
    }
}
