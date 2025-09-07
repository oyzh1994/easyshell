package cn.oyzh.easyshell.ssh2;

import org.apache.sshd.common.io.IoConnector;
import org.eclipse.jgit.internal.transport.sshd.JGitSshClient;

/**
 * jgit ssh客户端
 *
 * @author oyzh
 * @since 2025/07/01
 */
public class ShellSSHJGitClient extends JGitSshClient {

    /**
     * 代理端口
     */
    private int proxyPort;

    /**
     * 代理地址
     */
    private String proxyHost;

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    @Override
    public IoConnector createConnector() {
        return new ShellSSHIoConnector(this, super.createConnector());
    }
}