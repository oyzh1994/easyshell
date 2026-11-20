package cn.oyzh.easyshell.llm.mcp;

/**
 * MCP 客户端传输方式。
 *
 * @author oyzh
 * @since 2026-11-20
 */
public enum LLMMcpTransport {

    STDIO,

    STREAMABLE_HTTP,

    WEBSOCKET;

    public static LLMMcpTransport fromOrdinal(int ordinal) {
        LLMMcpTransport[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return STDIO;
        }
        return values[ordinal];
    }

    public boolean usesUrl() {
        return this != STDIO;
    }

    public String getDisplayName() {
        return switch (this) {
            case STDIO -> "stdio";
            case STREAMABLE_HTTP -> "Streamable HTTP";
            case WEBSOCKET -> "WebSocket";
        };
    }

    @Override
    public String toString() {
        return this.getDisplayName();
    }
}
