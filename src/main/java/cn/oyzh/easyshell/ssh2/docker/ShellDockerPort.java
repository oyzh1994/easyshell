package cn.oyzh.easyshell.ssh2.docker;

/**
 * docker端口信息
 *
 * @author oyzh
 * @since 2025-03-13
 */
public class ShellDockerPort {

    /**
     * 内部端口
     */
    private String innerPort;

    /**
     * 外部端口
     */
    private String outerPort;

    /**
     * 获取内部端口
     *
     * @return 内部端口
     */
    public String getInnerPort() {
        return innerPort;
    }

    /**
     * 设置内部端口
     *
     * @param innerPort 内部端口
     */
    public void setInnerPort(String innerPort) {
        this.innerPort = innerPort;
    }

    /**
     * 获取外部端口
     *
     * @return 外部端口
     */
    public String getOuterPort() {
        return outerPort;
    }

    /**
     * 设置外部端口
     *
     * @param outerPort 外部端口
     */
    public void setOuterPort(String outerPort) {
        this.outerPort = outerPort;
    }
}
