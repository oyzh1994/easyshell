package cn.oyzh.easyshell.llm.tool;

import cn.oyzh.easyshell.llm.LLMExecutionMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the tools exposed for one LLM request.
 *
 * <p>A bound tab, including a Local tab, is a SESSION. When a SESSION exists,
 * only SESSION and LOCAL_EXECUTION tools are exposed. Without a SESSION,
 * INTERNAL and LOCAL_EXECUTION tools are exposed.</p>
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMToolSelection {

    private final List<LLMTools> tools;

    private final boolean hasSession;

    private final String sessionSystemMessage;

    private final String internalSystemMessage;

    private final String localExecutionSystemMessage;

    public LLMToolSelection(LLMTools sessionTools,
                            LLMTools internalTools,
                            LLMTools localExecutionTools,
                            LLMExecutionMode executionMode) {
        if (localExecutionTools == null
                || localExecutionTools.scope() != LLMToolScope.LOCAL_EXECUTION) {
            throw new IllegalArgumentException("LOCAL_EXECUTION tools are required.");
        }
        if (sessionTools != null && internalTools != null) {
            throw new IllegalArgumentException(
                    "INTERNAL tools must not be combined with a SESSION.");
        }
        if (sessionTools == null && internalTools == null) {
            throw new IllegalArgumentException(
                    "Either SESSION or INTERNAL tools are required.");
        }
        if (sessionTools != null && sessionTools.scope() != LLMToolScope.SESSION) {
            throw new IllegalArgumentException("Bound tab tools must use SESSION scope.");
        }
        if (internalTools != null && internalTools.scope() != LLMToolScope.INTERNAL) {
            throw new IllegalArgumentException("Fallback tools must use INTERNAL scope.");
        }
        List<LLMTools> selected = new ArrayList<>(2);
        if (sessionTools != null) {
            selected.add(sessionTools);
            this.sessionSystemMessage = value(sessionTools.systemMessage(executionMode));
            this.hasSession = true;
        } else {
            selected.add(internalTools);
            this.sessionSystemMessage = "";
            this.hasSession = false;
        }
        selected.add(localExecutionTools);
        this.tools = List.copyOf(selected);
        this.internalSystemMessage = internalTools == null
                ? ""
                : value(internalTools.systemMessage(executionMode));
        this.localExecutionSystemMessage =
                value(localExecutionTools.systemMessage(executionMode));
    }

    public List<LLMTools> tools() {
        return this.tools;
    }

    public boolean hasSession() {
        return this.hasSession;
    }

    public String sessionSystemMessage() {
        return this.sessionSystemMessage;
    }

    public String internalSystemMessage() {
        return this.internalSystemMessage;
    }

    public String localExecutionSystemMessage() {
        return this.localExecutionSystemMessage;
    }

    public String systemMessage() {
        return joinSystemMessages(
                this.sessionSystemMessage,
                this.internalSystemMessage,
                this.localExecutionSystemMessage);
    }

    private static String joinSystemMessages(String... messages) {
        StringBuilder result = new StringBuilder();
        for (String message : messages) {
            if (message == null || message.isBlank()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append("\n\n");
            }
            result.append(message);
        }
        return result.toString();
    }

    private static String value(String message) {
        return message == null ? "" : message;
    }
}
