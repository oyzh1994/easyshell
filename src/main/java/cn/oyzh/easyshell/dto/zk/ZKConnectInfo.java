package cn.oyzh.easyshell.dto.zk;


/**
 * zk连接信息
 *
 * @author oyzh
 * @since 2023/9/20
 */
public class ZKConnectInfo {

    /**
     * 原始输入内容
     */
    private String input;

    /**
     * 地址
     */
    private String host = "localhost";

    /**
     * 端口
     */
    private int port = 2181;

    /**
     * 超时时间，单位毫秒
     */
    private int timeout = 5000;

    /**
     * 只读模式
     */
    private boolean readonly;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }
}
