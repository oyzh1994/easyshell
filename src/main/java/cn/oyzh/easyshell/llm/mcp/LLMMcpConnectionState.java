package cn.oyzh.easyshell.llm.mcp;

import cn.oyzh.i18n.I18nResourceBundle;

/**
 * MCP Server 连接检查状态。
 *
 * @author oyzh
 * @since 2026-11-20
 */
public enum LLMMcpConnectionState {

    UNKNOWN,

    CHECKING,

    CONNECTED,

    FAILED;

    public String getDisplayText(int toolCount) {
        return switch (this) {
            case UNKNOWN -> I18nResourceBundle.i18nString("shell.llm.mcp.connection.unknown");
            case CHECKING -> I18nResourceBundle.i18nString("shell.llm.mcp.connection.checking");
            case CONNECTED -> String.format(
                    I18nResourceBundle.i18nString("shell.llm.mcp.connection.connected"), toolCount);
            case FAILED -> I18nResourceBundle.i18nString("shell.llm.mcp.connection.failed");
        };
    }
}
