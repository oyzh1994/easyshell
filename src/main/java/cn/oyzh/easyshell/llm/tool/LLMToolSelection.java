package cn.oyzh.easyshell.llm.tool;

import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMUtil;

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

    private final LLMToolAccessMode accessMode;

    private final boolean hasSession;

    private final String sessionSystemMessage;

    private final String internalSystemMessage;

    private final String localExecutionSystemMessage;

    public LLMToolSelection(LLMTools sessionTools,
                            LLMTools internalTools,
                            LLMTools localExecutionTools,
                            LLMExecutionMode executionMode) {
        this(sessionTools, internalTools, localExecutionTools, executionMode,
                LLMToolAccessMode.SESSION_WITH_LOCAL);
    }

    public LLMToolSelection(LLMTools sessionTools,
                            LLMTools internalTools,
                            LLMTools localExecutionTools,
                            LLMExecutionMode executionMode,
                            LLMToolAccessMode accessMode) {
        this.accessMode = accessMode == null
                ? LLMToolAccessMode.SESSION_WITH_LOCAL
                : accessMode;
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
        if (sessionTools != null && internalTools != null
                && this.accessMode != LLMToolAccessMode.COMPLETE) {
            throw new IllegalArgumentException(
                    "SESSION and INTERNAL may only be combined in COMPLETE mode.");
        }
        if (localExecutionTools != null
                && localExecutionTools.scope() != LLMToolScope.LOCAL_EXECUTION) {
            throw new IllegalArgumentException("Local command tools must use LOCAL_EXECUTION scope.");
        }
        if (this.accessMode == LLMToolAccessMode.COMPLETE
                && sessionTools != null && internalTools == null) {
            throw new IllegalArgumentException(
                    "COMPLETE mode requires INTERNAL tools when a SESSION is bound.");
        }

        List<LLMTools> selected = new ArrayList<>(3);
        if (sessionTools != null) {
            selected.add(sessionTools);
            this.sessionSystemMessage = value(sessionTools.systemMessage(executionMode));
            this.hasSession = true;
        } else {
            selected.add(internalTools);
            this.sessionSystemMessage = "";
            this.hasSession = false;
        }
        boolean includeLocal = this.accessMode != LLMToolAccessMode.SESSION_ONLY;
        if (includeLocal) {
            if (localExecutionTools == null) {
                throw new IllegalArgumentException("LOCAL_EXECUTION tools are required.");
            }
            selected.add(localExecutionTools);
        }
        if (this.accessMode == LLMToolAccessMode.COMPLETE
                && sessionTools != null) {
            selected.add(internalTools);
        }
        this.tools = List.copyOf(selected);
        this.internalSystemMessage = internalTools == null || !selected.contains(internalTools)
                ? ""
                : value(internalTools.systemMessage(executionMode));
        this.localExecutionSystemMessage =
                localExecutionTools == null || !selected.contains(localExecutionTools)
                        ? ""
                        : value(localExecutionTools.systemMessage(executionMode));
    }

    public List<LLMTools> tools() {
        return this.tools;
    }

    public LLMToolAccessMode accessMode() {
        return this.accessMode;
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
        return LLMUtil.joinSystemMessages(
                this.sessionSystemMessage,
                this.internalSystemMessage,
                this.localExecutionSystemMessage);
    }

    private static String value(String message) {
        return message == null ? "" : message;
    }
}
