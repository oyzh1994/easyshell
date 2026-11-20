package cn.oyzh.easyshell.llm.mcp;

import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.Objects;

/**
 * MCP Server 配置。
 *
 * @author oyzh
 * @since 2026-11-20
 */
@Table("t_llm_mcp_server")
public class LLMMcpServer implements Serializable {

    @Column
    @PrimaryKey
    private String uid;

    @Column
    private String name;

    /**
     * 传输方式序号，见 {@link LLMMcpTransport}。
     */
    @Column
    private Integer transportType = LLMMcpTransport.STDIO.ordinal();

    /**
     * stdio 启动命令，支持引号参数。
     */
    @Column
    private String commandText;

    /**
     * stdio 进程工作目录。
     */
    @Column
    private String workingDirectory;

    /**
     * stdio 环境变量，每行一个 KEY=VALUE。
     */
    @Column
    private String environmentText;

    /**
     * HTTP/WebSocket 服务地址。
     */
    @Column
    private String url;

    /**
     * HTTP/WebSocket 请求头，每行一个 Name: Value。
     */
    @Column
    private String headersText;

    /**
     * 工具白名单，每行一个原始工具名；为空时启用全部工具。
     */
    @Column
    private String toolNamesText;

    /**
     * 初始化与工具调用超时秒数。
     */
    @Column
    private Integer timeoutSeconds = 60;

    @Column
    private Boolean enabled = true;

    @JSONField(serialize = false, deserialize = false)
    private transient LLMMcpConnectionState connectionState = LLMMcpConnectionState.UNKNOWN;

    @JSONField(serialize = false, deserialize = false)
    private transient String connectionMessage;

    @JSONField(serialize = false, deserialize = false)
    private transient int toolCount;

    @JSONField(serialize = false, deserialize = false)
    private transient boolean enabling;

    @JSONField(serialize = false, deserialize = false)
    private transient boolean checking;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTransportType() {
        return this.transportType == null ? LLMMcpTransport.STDIO.ordinal() : this.transportType;
    }

    public void setTransportType(int transportType) {
        this.transportType = transportType;
    }

    @JSONField(serialize = false, deserialize = false)
    public LLMMcpTransport getTransport() {
        return LLMMcpTransport.fromOrdinal(this.getTransportType());
    }

    @JSONField(serialize = false, deserialize = false)
    public void setTransport(LLMMcpTransport transport) {
        this.transportType = (transport == null ? LLMMcpTransport.STDIO : transport).ordinal();
    }

    public String getTransportName() {
        return this.getTransport().getDisplayName();
    }

    public String getCommandText() {
        return commandText;
    }

    public void setCommandText(String commandText) {
        this.commandText = commandText;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getEnvironmentText() {
        return environmentText;
    }

    public void setEnvironmentText(String environmentText) {
        this.environmentText = environmentText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHeadersText() {
        return headersText;
    }

    public void setHeadersText(String headersText) {
        this.headersText = headersText;
    }

    public String getToolNamesText() {
        return toolNamesText;
    }

    public void setToolNamesText(String toolNamesText) {
        this.toolNamesText = toolNamesText;
    }

    public int getTimeoutSeconds() {
        if (this.timeoutSeconds == null) {
            return 60;
        }
        return Math.max(1, Math.min(this.timeoutSeconds, 3600));
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = Math.max(1, Math.min(timeoutSeconds, 3600));
    }

    public boolean isEnabled() {
        return this.enabled == null || this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JSONField(serialize = false, deserialize = false)
    public LLMMcpConnectionState getConnectionState() {
        return this.connectionState == null ? LLMMcpConnectionState.UNKNOWN : this.connectionState;
    }

    @JSONField(serialize = false, deserialize = false)
    public void setConnectionState(LLMMcpConnectionState connectionState, int toolCount, String connectionMessage) {
        this.connectionState = connectionState == null ? LLMMcpConnectionState.UNKNOWN : connectionState;
        this.toolCount = toolCount;
        this.connectionMessage = connectionMessage;
    }

    @JSONField(serialize = false, deserialize = false)
    public void setConnectionState(LLMMcpConnectionState connectionState) {
        this.setConnectionState(connectionState, 0, null);
    }

    @JSONField(serialize = false, deserialize = false)
    public String getConnectionStatusText() {
        return this.getConnectionState().getDisplayText(this.toolCount);
    }

    @JSONField(serialize = false, deserialize = false)
    public String getConnectionMessage() {
        return this.connectionMessage;
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isEnabling() {
        return this.enabling;
    }

    @JSONField(serialize = false, deserialize = false)
    public void setEnabling(boolean enabling) {
        this.enabling = enabling;
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isChecking() {
        return this.checking;
    }

    @JSONField(serialize = false, deserialize = false)
    public void setChecking(boolean checking) {
        this.checking = checking;
    }

    /**
     * 表格展示用的连接目标。
     *
     * @return 连接目标
     */
    public String getEndpointText() {
        String value = this.getTransport() == LLMMcpTransport.STDIO
                ? this.commandText
                : this.url;
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }

    public boolean hasToolFilter() {
        return this.toolNamesText != null && !this.toolNamesText.isBlank();
    }

    public String getToolFilterText() {
        if (!this.hasToolFilter()) {
            return "";
        }
        return String.join(", ", LLMMcpConfigParser.parseToolNames(this.toolNamesText));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LLMMcpServer that)) {
            return false;
        }
        return Objects.equals(this.uid, that.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uid);
    }

    @Override
    public String toString() {
        return this.name == null ? "" : this.name;
    }
}
