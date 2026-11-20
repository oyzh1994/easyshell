package cn.oyzh.easyshell.llm.agent;

import cn.oyzh.i18n.I18nResourceBundle;

import java.util.Collection;

/**
 * Formats agent execution traces for the chat message component.
 *
 * @author oyzh
 * @since 2026-11-20
 */
public final class LLMAgentTraceFormatter {

    private static final int MAX_THINKING_LENGTH = 32 * 1024;

    private static final int MAX_CONCLUSION_LENGTH = 24 * 1024;

    private static final int MAX_TRACE_LENGTH = 128 * 1024;

    private LLMAgentTraceFormatter() {
    }

    public static String format(Collection<LLMAgentTrace> traces) {
        if (traces == null || traces.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (LLMAgentTrace trace : traces) {
            if (trace == null) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append("\n\n---\n\n");
            }
            result.append("### ").append(title(trace)).append('\n')
                    .append("- **").append(text("shell.llm.agent.trace.agent")).append(":** ")
                    .append(trace.agent()).append('\n')
                    .append("- **").append(text("shell.llm.agent.trace.objective")).append(":** ")
                    .append(trace.objective()).append('\n')
                    .append("- **").append(text("shell.llm.agent.trace.status")).append(":** ")
                    .append(status(trace.status())).append('\n')
                    .append("- **").append(text("shell.llm.agent.trace.duration")).append(":** ")
                    .append(trace.durationMillis()).append(" ms\n\n")
                    .append("**").append(text("shell.llm.thinkingLabel")).append("**\n\n");
            String thinking = limit(trace.thinking(), MAX_THINKING_LENGTH);
            result.append(thinking.isBlank()
                    ? text("shell.llm.agent.trace.noThinking")
                    : thinking);
            String conclusion = limit(trace.conclusion(), MAX_CONCLUSION_LENGTH);
            if (!conclusion.isBlank()) {
                result.append("\n\n**").append(text("shell.llm.answerLabel"))
                        .append("**\n\n").append(conclusion);
            }
            if (!trace.error().isBlank()) {
                result.append("\n\n**").append(text("shell.llm.agent.trace.error"))
                        .append("**\n\n").append(trace.error());
            }
            if (result.length() >= MAX_TRACE_LENGTH) {
                return limit(result.toString(), MAX_TRACE_LENGTH);
            }
        }
        return result.toString();
    }

    private static String title(LLMAgentTrace trace) {
        return switch (trace.type()) {
            case PLANNER -> text("shell.llm.agent.trace.planner");
            case WORKER -> String.format(text("shell.llm.agent.trace.worker"),
                    trace.stepIndex() + 1);
            case VERIFIER -> text("shell.llm.agent.trace.verifier");
        };
    }

    private static String status(LLMAgentTrace.Status status) {
        return switch (status) {
            case RUNNING -> text("shell.llm.agent.trace.status.running");
            case COMPLETED -> text("shell.llm.agent.trace.status.completed");
            case FAILED -> text("shell.llm.agent.trace.status.failed");
            case CANCELLED -> text("shell.llm.agent.trace.status.cancelled");
        };
    }

    private static String text(String key) {
        return I18nResourceBundle.i18nString(key);
    }

    private static String limit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "\n...[truncated]";
    }
}
