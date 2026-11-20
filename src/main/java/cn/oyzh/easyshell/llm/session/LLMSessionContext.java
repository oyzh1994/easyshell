package cn.oyzh.easyshell.llm.session;

import cn.oyzh.common.util.StringUtil;

/**
 * Immutable, non-sensitive context for the session bound to an LLM request.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public record LLMSessionContext(String id,
                                String name,
                                String type,
                                String host,
                                Integer port,
                                String user) {

    public static LLMSessionContext from(LLMSessionTab tab) {
        if (tab == null) {
            return null;
        }
        return new LLMSessionContext(
                value(tab.getId()),
                value(tab.getName()),
                value(tab.getType()),
                value(tab.getHost()),
                tab.getPort(),
                value(tab.getUser()));
    }

    public boolean available() {
        return StringUtil.isNotBlank(this.type)
                || StringUtil.isNotBlank(this.host)
                || StringUtil.isNotBlank(this.name);
    }

    public String promptText() {
        if (!this.available()) {
            return "";
        }
        String sessionType = StringUtil.isBlank(this.type) ? "unknown" : this.type;
        String target = "local".equalsIgnoreCase(sessionType)
                ? "the local machine running EasyShell"
                : "the currently bound " + sessionType + " session";
        return """
                ## Current bound LLM session (authoritative target)
                - sessionId: %s
                - type: %s
                - name: %s
                - host: %s
                - port: %s
                - user: %s
                - defaultTarget: %s

                Treat this context as the authoritative target for the current session.
                Resolve expressions such as "this server", "current server", "当前服务器",
                "这个连接", or "this tab" against this context.
                Do not infer the target from timezone, language, DNS, public IP, or conversation history.
                Never expose credentials or assume unlisted hosts.
                """.formatted(
                dash(this.id),
                sessionType,
                dash(this.name),
                dash(this.host),
                this.port == null || this.port <= 0 ? "-" : this.port,
                dash(this.user),
                target);
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    private static String dash(String value) {
        return StringUtil.isBlank(value) ? "-" : value;
    }
}
