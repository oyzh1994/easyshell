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
 *
 * @author oyzh
 * @since 2026-02-11
 */
public class ShellSSHKnownHostsServerKeyVerifier extends KnownHostsServerKeyVerifier {

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

    public static ShellSSHKnownHostsServerKeyVerifier INSTANCE = new ShellSSHKnownHostsServerKeyVerifier(new ShellSSHServerKeyVerifier(), ShellSSHUtil.getKnownHostsPath());
}
