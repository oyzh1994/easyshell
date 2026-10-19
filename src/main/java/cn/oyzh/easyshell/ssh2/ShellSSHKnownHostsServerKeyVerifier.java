package cn.oyzh.easyshell.ssh2;

import org.apache.sshd.client.config.hosts.KnownHostEntry;
import org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;

import java.net.SocketAddress;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Collection;

/**
 * ssh已知主机密钥校验器
 *
 * @author oyzh
 * @since 2026-02-11
 */
public class ShellSSHKnownHostsServerKeyVerifier extends KnownHostsServerKeyVerifier {

    /**
     * 构造ssh已知主机密钥校验器
     *
     * @param delegate 委托的密钥校验器
     * @param file     已知主机文件
     */
    public ShellSSHKnownHostsServerKeyVerifier(ServerKeyVerifier delegate, Path file) {
        super(delegate, file);
    }

    @Override
    protected KnownHostEntry updateKnownHostsFile(ClientSession clientSession, SocketAddress remoteAddress, PublicKey serverKey, Path file, Collection<HostEntryPair> knownHosts) throws Exception {
        //        if(SSHUtil.isMiddle(clientSession)){
        //            return null;
        //        }
        return super.updateKnownHostsFile(clientSession, remoteAddress, serverKey, file, knownHosts);
    }

    @Override
    public boolean verifyServerKey(ClientSession clientSession, SocketAddress remoteAddress, PublicKey serverKey) {
        return super.verifyServerKey(clientSession, remoteAddress, serverKey);
//        return true;
    }

    /**
     * 默认实例
     */
    public static ShellSSHKnownHostsServerKeyVerifier INSTANCE = new ShellSSHKnownHostsServerKeyVerifier(new ShellSSHServerKeyVerifier(), ShellSSHUtil.getKnownHostsPath());
}
