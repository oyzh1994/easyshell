package cn.oyzh.easyshell.rlogin;

import cn.oyzh.ssh.util.SSHUtil;
import org.apache.commons.net.bsd.RLoginClient;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * @author oyzh
 * @since 2026-09-21
 */
public class PatchedRLoginClient extends RLoginClient {

    @Override
    public void connect(InetAddress host, int port, InetAddress localAddr) throws IOException, IllegalArgumentException {
        int localPort = 0;
        Set<Integer> excludes = new HashSet<>();
        while (true) {
            try {
                localPort = SSHUtil.findAvailablePort(excludes);
                super._socket_ = super._socketFactory_.createSocket(host, port, localAddr, localPort);
                break;
            } catch (BindException ex) {
                excludes.add(localPort);
            }
        }
        super._connectAction_();
    }
}