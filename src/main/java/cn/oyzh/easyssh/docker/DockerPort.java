package cn.oyzh.easyssh.docker;

/**
 * @author oyzh
 * @since 2025-03-13
 */
public class DockerPort {

    private String innerPort;

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
