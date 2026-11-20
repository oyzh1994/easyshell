package cn.oyzh.easyshell.llm.session;

import cn.oyzh.easyshell.llm.tool.LLMTools;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * LLM会话绑定的Tab
 *
 * @author oyzh
 * @since 2026-10-30
 */
public class LLMSessionTab {

    /**
     * Tab id
     */
    private String id;

    /**
     * Tab名称
     */
    private String name;

    private String type;

    private String host;

    private Integer port;

    private String user;

    /**
     * 会话函数
     */
    private Supplier<? extends LLMTools> toolsFunc;

    public LLMSessionTab() {
    }

    public LLMSessionTab(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 获取Tab id
     *
     * @return Tab id
     */
    public String getId() {
        return id;
    }

    /**
     * 设置Tab id
     *
     * @param id Tab id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取Tab名称
     *
     * @return Tab名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置Tab名称
     *
     * @param name Tab名称
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Supplier<? extends LLMTools> getToolsFunc() {
        return toolsFunc;
    }

    public void setToolsFunc(Supplier<? extends LLMTools> toolsFunc) {
        this.toolsFunc = toolsFunc;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LLMSessionTab that)) {
            return false;
        }
        return Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name);
    }

    @Override
    public String toString() {
        return this.name == null ? "" : this.name;
    }
}
