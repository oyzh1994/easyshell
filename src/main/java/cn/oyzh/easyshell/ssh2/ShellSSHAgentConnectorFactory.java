package cn.oyzh.easyshell.ssh2;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import org.eclipse.jgit.internal.transport.sshd.agent.connector.PageantConnector;
import org.eclipse.jgit.internal.transport.sshd.agent.connector.UnixDomainSocketConnector;
import org.eclipse.jgit.transport.sshd.agent.Connector;
import org.eclipse.jgit.transport.sshd.agent.ConnectorFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * ssh agent连接工厂
 *
 * @author oyzh
 * @since 2025-06-30
 */
public class ShellSSHAgentConnectorFactory implements ConnectorFactory {

    /**
     * 当前实例
     */
    public static final ShellSSHAgentConnectorFactory INSTANCE = new ShellSSHAgentConnectorFactory();

    @Override
    public Connector create(String identityAgent, File homeDir) throws IOException {
        if (OSUtil.isWindows()) {
            return new PageantConnector();
        }
        if (StringUtil.isBlank(identityAgent)) {
            identityAgent = ShellSSHUtil.getSSHAgentSockFile();
        }
        return new UnixDomainSocketConnector(identityAgent);
    }

    @Override
    public boolean isSupported() {
        return false;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Collection<ConnectorDescriptor> getSupportedConnectors() {
        return List.of();
    }

    @Override
    public ConnectorDescriptor getDefaultConnector() {
        return null;
    }
}
