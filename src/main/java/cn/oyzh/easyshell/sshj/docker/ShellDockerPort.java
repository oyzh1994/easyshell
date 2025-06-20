package cn.oyzh.easyshell.sshj.docker;

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

    public String getInnerPort() {
        return innerPort;
    }

    public void setInnerPort(String innerPort) {
        this.innerPort = innerPort;
    }

    public String getOuterPort() {
        return outerPort;
    }

    public void setOuterPort(String outerPort) {
        this.outerPort = outerPort;
    }
}
