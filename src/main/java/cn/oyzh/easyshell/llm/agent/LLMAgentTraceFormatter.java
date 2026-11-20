package cn.oyzh.easyshell.llm.agent;

import cn.oyzh.i18n.I18nResourceBundle;

import java.util.Collection;
import java.util.Locale;

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
            result.append("### ").append(title(trace))
                    .append(" · ").append(status(trace.status())).append('\n')
                    .append("- **").append(text("shell.llm.agent.trace.agent")).append(":** ")
                    .append(trace.agent()).append('\n')
                    .append("- **").append(text("shell.llm.agent.trace.objective")).append(":** ")
                    .append(trace.objective()).append('\n')
                    .append("- **").append(text("shell.llm.agent.trace.duration")).append(":** ")
                    .append(formatDuration(trace.durationMillis())).append('\n');
            String thinking = limit(trace.thinking(), MAX_THINKING_LENGTH);
            if (!thinking.isBlank()) {
                result.append("\n\n**").append(text("shell.llm.thinkingLabel"))
                        .append("**\n\n").append(thinking);
            } else if (trace.status() != LLMAgentTrace.Status.RUNNING) {
                result.append("\n\n**").append(text("shell.llm.thinkingLabel"))
                        .append("**\n\n").append(text("shell.llm.agent.trace.noThinking"));
            }
            String conclusion = limit(trace.conclusion(), MAX_CONCLUSION_LENGTH);
            if (!conclusion.isBlank()) {
                result.append("\n\n**").append(text("shell.llm.agent.trace.result"))
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

    private static String formatDuration(long millis) {
        long value = Math.max(0, millis);
        if (value < 1_000) {
            return value + " ms";
        }
        if (value < 60_000) {
            return String.format(Locale.ROOT, "%.1f s", value / 1_000.0);
        }
        long totalSeconds = value / 1_000;
        return String.format(Locale.ROOT, "%02d:%02d",
                totalSeconds / 60, totalSeconds % 60);
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
