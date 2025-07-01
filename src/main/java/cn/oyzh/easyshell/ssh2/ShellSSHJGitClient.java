package cn.oyzh.easyshell.ssh2;

import cn.oyzh.easyshell.domain.ShellProxyConfig;
import org.apache.sshd.client.config.hosts.HostConfigEntry;
import org.apache.sshd.client.session.ClientProxyConnector;
import org.apache.sshd.client.session.ClientSessionImpl;
import org.apache.sshd.client.session.SessionFactory;
import org.apache.sshd.common.AttributeRepository;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoConnectFuture;
import org.apache.sshd.common.io.IoConnector;
import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.common.io.IoServiceEventListener;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.eclipse.jgit.internal.transport.sshd.JGitClientSession;
import org.eclipse.jgit.internal.transport.sshd.JGitSshClient;
import org.eclipse.jgit.internal.transport.sshd.proxy.AbstractClientProxyConnector;
import org.eclipse.jgit.internal.transport.sshd.proxy.HttpClientConnector;
import org.eclipse.jgit.internal.transport.sshd.proxy.Socks5ClientConnector;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class ShellSSHJGitClient extends JGitSshClient {


    private ShellProxyConfig proxyConfig;

    public ShellProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    public void setProxyConfig(ShellProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    private static final Field HOST_CONFIG_ENTRY_FIELD;
    private static final AttributeRepository.AttributeKey<HostConfigEntry> HOST_CONFIG_ENTRY;
    private static final String CLIENT_PROXY_CONNECTOR = "ClientProxyConnectorId";

    static {
        try {
            HOST_CONFIG_ENTRY_FIELD = JGitSshClient.class.getDeclaredField("HOST_CONFIG_ENTRY");
            HOST_CONFIG_ENTRY_FIELD.setAccessible(true);
            HOST_CONFIG_ENTRY = (AttributeRepository.AttributeKey<HostConfigEntry>) HOST_CONFIG_ENTRY_FIELD.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final ShellSSHJGitClient sshClient = this;
    private final ConcurrentHashMap<String, ClientProxyConnector> clientProxyConnectors = new ConcurrentHashMap<>();

    @Override
    public IoConnector createConnector() {
        return new MyIoConnector(this, super.createConnector());
    }

    // @Override
    // public SessionFactory createSessionFactory() {
    //     return new SessionFactory(sshClient) {
    //         @Override
    //         protected ClientSessionImpl doCreateSession(IoSession ioSession) throws Exception {
    //             return new JGitClientSession(sshClient, ioSession) {
    //                 @Override
    //                 public ClientProxyConnector getClientProxyConnector() {
    //                     HostConfigEntry entry = getAttribute(HOST_CONFIG_ENTRY);
    //                     if (entry == null) {
    //                         return null;
    //                     }
    //                     String clientProxyConnectorId = entry.getProperty(CLIENT_PROXY_CONNECTOR);
    //                     if (clientProxyConnectorId == null) {
    //                         return null;
    //                     }
    //                     ClientProxyConnector clientProxyConnector = sshClient.clientProxyConnectors.get(clientProxyConnectorId);
    //
    //                     if (clientProxyConnector != null) {
    //                         addSessionListener(new SessionListener() {
    //                             @Override
    //                             public void sessionClosed(Session session) {
    //                                 clientProxyConnectors.remove(clientProxyConnectorId);
    //                             }
    //                         });
    //                     }
    //
    //                     return clientProxyConnector;
    //                 }
    //
    //             };
    //         }
    //     };
    // }

    // @Override
    // public void setClientProxyConnector(ClientProxyConnector proxyConnector) {
    //     super.setClientProxyConnector(proxyConnector);
    // }

    private static class MyIoConnector implements IoConnector {

        private final ShellSSHJGitClient sshClient;
        private final IoConnector ioConnector;

        public MyIoConnector(ShellSSHJGitClient sshClient, IoConnector ioConnector) {
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
            SocketAddress tAddress = targetAddress;
            // HostConfigEntry entry = context != null ? context.getAttribute(HOST_CONFIG_ENTRY) : null;
            // if (entry != null) {
            //     Host host = HostManager.getHost(entry.getProperty("Host") != null ? entry.getProperty("Host") : "");
            //     if (host != null) {
            //         tAddress = configureProxy(entry, host, tAddress);
            //     }
            // }
                if(sshClient.getProxyConfig()!=null){
                    ShellProxyConfig config=this.sshClient.proxyConfig;
                    tAddress=  new InetSocketAddress(config.getHost(), config.getPort());
                }
            return ioConnector.connect(tAddress, context, localAddress);
        }

        private SocketAddress configureProxy(HostConfigEntry entry, Host host, SocketAddress targetAddress) {
            if (host.proxyType.equals("NONE")) {
                return targetAddress;
            }
            if (!(targetAddress instanceof InetSocketAddress)) {
                return targetAddress;
            }
            InetSocketAddress address = (InetSocketAddress) targetAddress;
            if (address.getHostString().equals(SshdSocketAddress.LOCALHOST_IPV4)) {
                return targetAddress;
            }

            // 获取代理连接器
            AbstractClientProxyConnector clientProxyConnector = getClientProxyConnector(host, address);
            if (clientProxyConnector == null) {
                return targetAddress;
            }

            String id = UUID.randomUUID().toString().replace("-", "");
            entry.setProperty(CLIENT_PROXY_CONNECTOR, id);
            sshClient.clientProxyConnectors.put(id, clientProxyConnector);

            return new InetSocketAddress(host.proxyHost, host.proxyPort);
        }

        private AbstractClientProxyConnector getClientProxyConnector(Host host, InetSocketAddress remoteAddress) {
            if (host.proxyType.equals("HTTP")) {
                return new HttpClientConnector(
                        new InetSocketAddress(host.proxyHost, host.proxyPort),
                        remoteAddress,
                        host.proxyUser != null && !host.proxyUser.isEmpty() ? host.proxyUser : null,
                        host.proxyPassword != null && !host.proxyPassword.isEmpty() ? host.proxyPassword.toCharArray() : null
                );
            } else if (host.proxyType.equals("SOCKS5")) {
                return new Socks5ClientConnector(
                        new InetSocketAddress(host.proxyHost, host.proxyPort),
                        remoteAddress,
                        host.proxyUser != null && !host.proxyUser.isEmpty() ? host.proxyUser : null,
                        host.proxyPassword != null && !host.proxyPassword.isEmpty() ? host.proxyPassword.toCharArray() : null

                );
            }
            return null;
        }
    }

}