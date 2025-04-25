package cn.oyzh.easyshell.telnet;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.BaseClient;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class TelnetClient extends org.apache.commons.net.telnet.TelnetClient implements BaseClient {

    private final ShellConnect shellConnect;

    public TelnetClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    @Override
    public void start(int timeout) throws IOException {
        this.setDefaultTimeout(timeout);
        this.setConnectTimeout(timeout);
        String charset = this.shellConnect.getCharset();
        if (StringUtil.isNotEmpty(charset)) {
            this.setCharset(Charset.forName(charset));
        }
        super.connect(this.shellConnect.hostIp(), this.shellConnect.hostPort());
    }

    @Override
    public void close() {
        try {
            this.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }
}
