package cn.oyzh.easyshell.rsh;

import cn.oyzh.ssh.util.SSHUtil;
import org.apache.commons.net.bsd.RCommandClient;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * 修正版rsh客户端，非标准端口场景下允许使用普通本地端口。
 *
 * @author oyzh
 * @since 2026-11-14
 */
public class PatchedRCommandClient extends RCommandClient {

    @Override
    public void connect(InetAddress host, int port, InetAddress localAddr) throws IOException, IllegalArgumentException {
        int localPort = 0;
        Set<Integer> excludes = new HashSet<>();
        while (true) {
            localPort = SSHUtil.findAvailablePort(excludes);
            if (localPort < 0) {
                throw new BindException("No available local port.");
            }
            try {
                super._socket_ = super._socketFactory_.createSocket(host, port, localAddr, localPort);
                break;
            } catch (BindException ex) {
                try {
                    super._socket_ = super._socketFactory_.createSocket(host, port, null, localPort);
                    break;
                } catch (BindException ex2) {
                    excludes.add(localPort);
                }
            }
        }
        super._connectAction_();
    }
}
