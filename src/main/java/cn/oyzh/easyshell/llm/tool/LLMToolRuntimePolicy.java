package cn.oyzh.easyshell.llm.tool;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.session.LLMSessionContext;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Runtime policy for ordinary, MCP and Skill tool execution.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMToolRuntimePolicy {

    public static final String BLOCKED_PREFIX = "[EASYSHELL_POLICY_BLOCKED] ";

    private final LLMExecutionMode executionMode;

    private final LLMToolAccessMode accessMode;

    private final LLMSessionContext session;

    private final String task;

    private final Map<String, LLMToolScope> toolScopes;

    private final Set<LLMToolScope> exposedScopes;

    private final boolean localRequested;

    public LLMToolRuntimePolicy(LLMExecutionMode executionMode,
                                LLMToolAccessMode accessMode,
                                LLMSessionContext session,
                                String task,
                                Map<String, LLMToolScope> toolScopes) {
        this.executionMode = executionMode == null
                ? LLMExecutionMode.APPROVE
                : executionMode;
        this.accessMode = accessMode == null
                ? LLMToolAccessMode.SESSION_WITH_LOCAL
                : accessMode;
        this.session = session;
        this.task = task == null ? "" : task;
        this.toolScopes = toolScopes == null ? Map.of() : Map.copyOf(toolScopes);
        this.localRequested = requestsLocalMachine(this.task);
        this.exposedScopes = this.resolveExposedScopes();
    }

    /**
     * Checks whether a tool may execute.
     *
     * @param toolName tool name
     * @param scope    tool scope
     * @return policy decision
     */
    public Decision check(String toolName, LLMToolScope scope) {
        if (this.executionMode == LLMExecutionMode.SUGGEST) {
            return Decision.deny("""
                    Execution blocked by EasyShell policy: the current mode is Suggest.
                    Do not execute tools. Provide analysis, a concrete command or an execution plan only.
                    Switch to Approve or Unrestricted mode to execute it.
                    """);
        }
        if (scope == LLMToolScope.LOCAL_EXECUTION
                && this.hasNonLocalSession()
                && !this.localRequested) {
            return Decision.deny("""
                    Execution blocked by EasyShell policy: the current bound target is %s.
                    Do not run this operation on the machine running EasyShell.
                    Use `%s` for the current session.
                    If the local machine is genuinely intended, explicitly say "本机/本地机器" or
                    "local machine", or switch to a Local tab.
                    """.formatted(this.targetDescription(), this.preferredSessionTool()));
        }
        if (scope == null || !this.exposedScopes.contains(scope)) {
            return Decision.deny(this.scopeDeniedMessage(scope));
        }
        return Decision.allow();
    }

    public boolean exposes(LLMToolScope scope) {
        return scope != null && this.exposedScopes.contains(scope);
    }

    public LLMToolScope scopeOf(String toolName) {
        return toolName == null ? null : this.toolScopes.get(toolName);
    }

    public boolean localRequested() {
        return this.localRequested;
    }

    public LLMExecutionMode executionMode() {
        return this.executionMode;
    }

    public LLMToolAccessMode accessMode() {
        return this.accessMode;
    }

    /**
     * Returns the high-priority prompt describing hard runtime constraints.
     *
     * @return policy prompt
     */
    public String promptText() {
        if (this.executionMode == LLMExecutionMode.SUGGEST) {
            return """
                    ## Runtime tool policy
                    Tools are disabled in Suggest mode. Do not claim that an operation was executed.
                    Provide analysis and commands only.
                    """;
        }
        if (this.hasNonLocalSession() && !this.localRequested) {
            return """
                    ## Runtime tool policy
                    Local-machine execution tools are hidden for this request because the bound target is %s.
                    Use `%s` for operations in the current session.
                    Local tools become available only when the user explicitly requests "本机/本地机器"
                    or "local machine", or switches to a Local tab.
                    """.formatted(this.targetDescription(), this.preferredSessionTool());
        }
        return "";
    }

    private Set<LLMToolScope> resolveExposedScopes() {
        if (this.executionMode == LLMExecutionMode.SUGGEST) {
            return Set.of();
        }
        Set<LLMToolScope> scopes = EnumSet.noneOf(LLMToolScope.class);
        switch (this.accessMode) {
            case SESSION_WITH_LOCAL -> {
                scopes.add(this.hasSession() ? LLMToolScope.SESSION : LLMToolScope.INTERNAL);
                scopes.add(LLMToolScope.LOCAL_EXECUTION);
            }
            case SESSION_ONLY -> scopes.add(
                    this.hasSession() ? LLMToolScope.SESSION : LLMToolScope.INTERNAL);
            case COMPLETE -> {
                scopes.add(this.hasSession() ? LLMToolScope.SESSION : LLMToolScope.INTERNAL);
                if (this.hasSession()) {
                    scopes.add(LLMToolScope.INTERNAL);
                }
                scopes.add(LLMToolScope.LOCAL_EXECUTION);
            }
        }
        scopes.add(LLMToolScope.MCP);
        scopes.add(LLMToolScope.SKILL);
        if (this.hasNonLocalSession() && !this.localRequested) {
            scopes.remove(LLMToolScope.LOCAL_EXECUTION);
        }
        return Set.copyOf(scopes);
    }

    private String scopeDeniedMessage(LLMToolScope scope) {
        String name = scope == null ? "unknown" : scope.name();
        return """
                Execution blocked by EasyShell policy: tool scope %s is not available for this request.
                Current target: %s.
                Use only tools exposed for the current bound session and selected tool scope.
                """.formatted(name, this.targetDescription());
    }

    private String preferredSessionTool() {
        String type = this.session == null ? "" : this.session.type().toLowerCase(Locale.ROOT);
        String expected = type.isBlank() ? "execute_command" : type + "_execute_command";
        String fallback = null;
        for (Map.Entry<String, LLMToolScope> entry : this.toolScopes.entrySet()) {
            if (entry.getValue() != LLMToolScope.SESSION) {
                continue;
            }
            if (entry.getKey().equalsIgnoreCase(expected)) {
                return entry.getKey();
            }
            if (fallback == null && entry.getKey().contains("execute_command")) {
                fallback = entry.getKey();
            }
        }
        return fallback == null ? "a current-session command tool" : fallback;
    }

    private String targetDescription() {
        if (!this.hasSession()) {
            return "the local machine running EasyShell";
        }
        String type = value(this.session.type(), "session");
        String target = value(this.session.host(), this.session.name());
        String user = value(this.session.user(), "");
        if (target.isBlank()) {
            return type;
        }
        String identity = user.isBlank() ? target : user + "@" + target;
        return type + " " + identity;
    }

    private boolean hasSession() {
        return this.session != null && this.session.available();
    }

    private boolean hasNonLocalSession() {
        if (!this.hasSession()) {
            return false;
        }
        String type = value(this.session.type(), "").toLowerCase(Locale.ROOT);
        return !type.contains("local");
    }

    private static boolean requestsLocalMachine(String task) {
        if (StringUtil.isBlank(task)) {
            return false;
        }
        String text = task.toLowerCase(Locale.ROOT);
        return text.contains("本机")
                || text.contains("这台电脑")
                || text.contains("当前电脑")
                || text.contains("当前机器")
                || text.contains("本地机器")
                || text.contains("本地电脑")
                || text.contains("本地环境")
                || text.contains("本地安装")
                || text.contains("本地执行")
                || text.contains("本地操作")
                || text.contains("本地主机")
                || text.contains("在本地")
                || text.contains("到本地")
                || text.contains("运行 easyshell 的机器")
                || text.contains("运行easyshell的机器")
                || text.contains("local machine")
                || text.contains("this machine")
                || text.contains("my machine")
                || text.contains("host machine")
                || text.contains("locally");
    }

    private static String value(String value, String fallback) {
        return StringUtil.isBlank(value) ? fallback : value.trim();
    }

    public record Decision(boolean allowed, String blockReason) {

        static Decision allow() {
            return new Decision(true, "");
        }

        static Decision deny(String reason) {
            return new Decision(false, BLOCKED_PREFIX + reason.strip());
        }
    }
}
