package cn.oyzh.easyshell.llm.tool;

/**
 * Execution scope of an LLM tool group.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public enum LLMToolScope {

    /**
     * Tools supplied by the currently bound tab. A Local tab is also a session.
     */
    SESSION,

    /**
     * EasyShell application diagnostics and management tools.
     */
    INTERNAL,

    /**
     * Commands executed on the machine running EasyShell.
     */
    LOCAL_EXECUTION,

    /**
     * Tools supplied by external MCP servers.
     */
    MCP
}
