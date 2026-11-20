package cn.oyzh.easyshell.llm.agent;

/**
 * Immutable snapshot of one composite-agent execution stage.
 *
 * @author oyzh
 * @since 2026-11-20
 */
public record LLMAgentTrace(String id,
                            Type type,
                            int stepIndex,
                            int totalSteps,
                            String agent,
                            String objective,
                            String thinking,
                            String conclusion,
                            String error,
                            Status status,
                            long startedAt,
                            long finishedAt) {

    public LLMAgentTrace {
        agent = value(agent);
        objective = value(objective);
        thinking = value(thinking);
        conclusion = value(conclusion);
        error = value(error);
        status = status == null ? Status.RUNNING : status;
    }

    public static LLMAgentTrace running(String id,
                                        Type type,
                                        int stepIndex,
                                        int totalSteps,
                                        String agent,
                                        String objective) {
        return new LLMAgentTrace(id, type, stepIndex, totalSteps, agent, objective,
                "", "", "", Status.RUNNING, System.currentTimeMillis(), 0);
    }

    public LLMAgentTrace withThinking(String value) {
        return new LLMAgentTrace(this.id, this.type, this.stepIndex, this.totalSteps,
                this.agent, this.objective, value, this.conclusion, this.error,
                this.status, this.startedAt, this.finishedAt);
    }

    public LLMAgentTrace withConclusion(String value) {
        return new LLMAgentTrace(this.id, this.type, this.stepIndex, this.totalSteps,
                this.agent, this.objective, this.thinking, value, this.error,
                this.status, this.startedAt, this.finishedAt);
    }

    public LLMAgentTrace withError(String value) {
        return new LLMAgentTrace(this.id, this.type, this.stepIndex, this.totalSteps,
                this.agent, this.objective, this.thinking, this.conclusion, value,
                this.status, this.startedAt, this.finishedAt);
    }

    public LLMAgentTrace complete(String value) {
        return new LLMAgentTrace(this.id, this.type, this.stepIndex, this.totalSteps,
                this.agent, this.objective, this.thinking, value, this.error,
                Status.COMPLETED, this.startedAt, System.currentTimeMillis());
    }

    public LLMAgentTrace fail(String value) {
        return new LLMAgentTrace(this.id, this.type, this.stepIndex, this.totalSteps,
                this.agent, this.objective, this.thinking, this.conclusion, value,
                Status.FAILED, this.startedAt, System.currentTimeMillis());
    }

    public LLMAgentTrace cancel() {
        return new LLMAgentTrace(this.id, this.type, this.stepIndex, this.totalSteps,
                this.agent, this.objective, this.thinking, this.conclusion, this.error,
                Status.CANCELLED, this.startedAt, System.currentTimeMillis());
    }

    public long durationMillis() {
        return this.finishedAt > this.startedAt
                ? this.finishedAt - this.startedAt
                : Math.max(0, System.currentTimeMillis() - this.startedAt);
    }

    private static String value(String text) {
        return text == null ? "" : text;
    }

    public enum Type {

        SINGLE,

        PLANNER,

        WORKER,

        VERIFIER
    }

    public enum Status {

        RUNNING,

        COMPLETED,

        FAILED,

        CANCELLED
    }
}
