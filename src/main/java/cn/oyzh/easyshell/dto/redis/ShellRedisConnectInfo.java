package cn.oyzh.easyshell.dto.redis;


/**
 * redis连接
 *
 * @author oyzh
 * @since 2023/8/10
 */
public class ShellRedisConnectInfo {

    /**
     * 原始输入内容
     */
    private String input;

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

    /** 获取用户 */
    public String getUser() {
        return user;
    }

    /** 设置用户 */
    public void setUser(String user) {
        this.user = user;
    }

    /** 获取密码 */
    public String getPassword() {
        return password;
    }

    /** 设置密码 */
    public void setPassword(String password) {
        this.password = password;
    }

    /** 获取db索引 */
    public int getDb() {
        return db;
    }

    /** 设置db索引 */
    public void setDb(int db) {
        this.db = db;
    }

    /** 是否只读模式 */
    public boolean isReadonly() {
        return readonly;
    }

    /** 设置是否只读模式 */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    /**
     * 地址
     */
    private String host = "127.0.0.1";

    /**
     * 端口
     */
    private int port = 6379;

    /**
     * 超时时间
     */
    private int timeout = 3000;

    /**
     * 用户
     */
    private String user;

    /**
     * 密码
     */
    private String password;

    /**
     * db索引
     */
    private int db = 0;

    /**
     * 只读模式
     */
    private boolean readonly;
}
