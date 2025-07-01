package cn.oyzh.easyshell.ssh2;

import org.apache.sshd.common.AttributeRepository;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoConnectFuture;
import org.apache.sshd.common.io.IoConnector;
import org.apache.sshd.common.io.IoServiceEventListener;
import org.apache.sshd.common.io.IoSession;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

/**
 * ssh io处理器
 *
 * @author oyzh
 * @since 2025/07/01
 */
public class ShellSSHIoConnector implements IoConnector {

    private final IoConnector ioConnector;

    private final ShellSSHJGitClient sshClient;

    public ShellSSHIoConnector(ShellSSHJGitClient sshClient, IoConnector ioConnector) {
        this.sshClient = sshClient;
        this.ioConnector = ioConnector;
    }

    @Override
    public CloseFuture close(boolean immediately) {
        return ioConnector.close(immediately);
    }

    @Override
    public void addCloseFutureListener(SshFutureListener<CloseFuture> listener) {
        ioConnector.addCloseFutureListener(listener);
    }

    @Override
    public void removeCloseFutureListener(SshFutureListener<CloseFuture> listener) {
        ioConnector.removeCloseFutureListener(listener);
    }

    @Override
    public boolean isClosed() {
        return ioConnector.isClosed();
    }

    @Override
    public boolean isClosing() {
        return ioConnector.isClosing();
    }

    @Override
    public IoServiceEventListener getIoServiceEventListener() {
        return ioConnector.getIoServiceEventListener();
    }

    @Override
    public void setIoServiceEventListener(IoServiceEventListener listener) {
        ioConnector.setIoServiceEventListener(listener);
    }

    @Override
    public Map<Long, IoSession> getManagedSessions() {
        return ioConnector.getManagedSessions();
    }

    @Override
    public IoConnectFuture connect(SocketAddress targetAddress, AttributeRepository context, SocketAddress localAddress) {
        // 如果有代理参数，则更改目标端口
        if (this.sshClient.getProxyHost() != null) {
            targetAddress = new InetSocketAddress(this.sshClient.getProxyHost(), this.sshClient.getProxyPort());
        }
        return this.ioConnector.connect(targetAddress, context, localAddress);
    }
}