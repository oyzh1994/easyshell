package cn.oyzh.easyshell.llm.tool;

/**
 * Controls which ordinary tool scopes are exposed to one LLM request.
 *
 * <p>MCP tools are independent and are not part of this mode.</p>
 *
 * @author oyzh
 * @since 2026-11-21
 */
public enum LLMToolAccessMode {

    /**
     * Current session plus local execution tools.
     */
    SESSION_WITH_LOCAL,

    /**
     * Current session tools only. When no session is bound, internal tools only.
     */
    SESSION_ONLY,

    /**
     * Current session, local execution, and EasyShell internal tools.
     * Other session tabs are never included.
     */
    COMPLETE;

    public static LLMToolAccessMode fromOrdinal(int ordinal) {
        LLMToolAccessMode[] modes = values();
        return ordinal < 0 || ordinal >= modes.length
                ? SESSION_WITH_LOCAL
                : modes[ordinal];
    }
}
