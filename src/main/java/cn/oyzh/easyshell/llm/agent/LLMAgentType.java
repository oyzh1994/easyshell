package cn.oyzh.easyshell.llm.agent;

import java.util.Locale;

/**
 * Agent worker tool domain.
 *
 * @author oyzh
 * @since 2026-11-20
 */
public enum LLMAgentType {

    CONNECTION,

    INTERNAL,

    MCP;

    public static LLMAgentType from(String value) {
        if (value == null) {
            return CONNECTION;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "internal", "application", "app" -> INTERNAL;
            case "mcp", "external" -> MCP;
            default -> CONNECTION;
        };
    }
}
