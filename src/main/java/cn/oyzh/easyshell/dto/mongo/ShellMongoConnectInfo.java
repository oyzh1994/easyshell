package cn.oyzh.easyshell.dto.mongo;


/**
 * mongodb连接信息
 *
 * @author oyzh
 * @since 2023/9/20
 */
public class ShellMongoConnectInfo {

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

    /** 获取原始输入内容 */
    public String getInput() {
        return input;
    }

    /** 设置原始输入内容 */
    public void setInput(String input) {
        this.input = input;
    }

    /** 获取地址 */
    public String getHost() {
        return host;
    }

    /** 设置地址 */
    public void setHost(String host) {
        this.host = host;
    }

    /** 获取端口 */
    public int getPort() {
        return port;
    }

    /** 设置端口 */
    public void setPort(int port) {
        this.port = port;
    }

    /** 获取超时时间 */
    public int getTimeout() {
        return timeout;
    }

    /** 设置超时时间 */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /** 是否只读模式 */
    public boolean isReadonly() {
        return readonly;
    }

    /** 设置是否只读模式 */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }
}
