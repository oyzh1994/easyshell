package cn.oyzh.easyshell.rlogin;

import cn.oyzh.ssh.util.SSHUtil;
import org.apache.commons.net.bsd.RLoginClient;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author oyzh
 * @since 2026-09-21
 */
public class PatchedRLoginClient extends RLoginClient {

    @Override
    public void connect(InetAddress host, int port, InetAddress localAddr) throws IOException, IllegalArgumentException {
        int localPort = SSHUtil.findAvailablePort();
        super._socket_ = super._socketFactory_.createSocket(host, port, localAddr, localPort);
        super._connectAction_();
    }
}