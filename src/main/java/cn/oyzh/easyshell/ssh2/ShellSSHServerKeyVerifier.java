package cn.oyzh.easyshell.ssh2;

import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import org.apache.sshd.client.config.hosts.KnownHostEntry;
import org.apache.sshd.client.keyverifier.ModifiedServerKeyAcceptor;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;

import java.net.SocketAddress;
import java.security.PublicKey;

/**
 *
 * @author oyzh
 * @since 2026-02-11
 */
public class ShellSSHServerKeyVerifier implements ServerKeyVerifier, ModifiedServerKeyAcceptor {

    @Override
    public boolean verifyServerKey(ClientSession clientSession, SocketAddress remoteAddress, PublicKey serverKey) {
//        if (SSHUtil.isMiddle(clientSession)) {
//            return true;
//        }
        return MessageBox.confirm(ShellI18nHelper.sshTip2());
    }

    @Override
    public boolean acceptModifiedServerKey(ClientSession clientSession, SocketAddress remoteAddress, KnownHostEntry entry, PublicKey expected, PublicKey actual) throws Exception {
//        if (SSHUtil.isMiddle(clientSession)) {
//            return true;
//        }
        return MessageBox.confirm(ShellI18nHelper.sshTip1());
    }
}
