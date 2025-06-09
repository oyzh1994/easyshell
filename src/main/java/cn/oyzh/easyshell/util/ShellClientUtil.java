package cn.oyzh.easyshell.util;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.ftp.ShellFTPClient;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.easyshell.serial.ShellSerialClient;
import cn.oyzh.easyshell.ssh.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.telnet.ShellTelnetClient;


/**
 * @author oyzh
 * @since 2025-04-25
 */
public class ShellClientUtil {

    public static <T extends BaseClient> T newClient(ShellConnect connect) {
        BaseClient client = null;
        if (connect.isSSHType()) {
            client = new ShellSSHClient(connect);
        } else if (connect.isSFTPType()) {
            client = new ShellSFTPClient(connect);
        } else if (connect.isFTPType()) {
            client = new ShellFTPClient(connect);
        } else if (connect.isSerialType()) {
            client = new ShellSerialClient(connect);
        } else if (connect.isTelnetType()) {
            client = new ShellTelnetClient(connect);
        }
        return (T) client;
    }

}
