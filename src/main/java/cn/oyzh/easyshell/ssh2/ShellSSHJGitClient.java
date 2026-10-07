package cn.oyzh.easyshell.ssh2;

import org.apache.sshd.common.io.IoConnector;
import org.eclipse.jgit.internal.transport.sshd.JGitSshClient;

/**
 * jgit ssh客户端
 *
 * @author oyzh
 * @since 2025-07-01
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

    /**
     * 获取代理端口
     *
     * @return 代理端口
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * 设置代理端口
     *
     * @param proxyPort 代理端口
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * 获取代理地址
     *
     * @return 代理地址
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * 设置代理地址
     *
     * @param proxyHost 代理地址
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    @Override
    public IoConnector createConnector() {
        return new ShellSSHIoConnector(this, super.createConnector());
    }
}