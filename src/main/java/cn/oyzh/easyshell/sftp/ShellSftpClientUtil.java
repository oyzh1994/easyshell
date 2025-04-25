package cn.oyzh.easyshell.sftp;

import cn.oyzh.easyshell.domain.ShellConnect;


/**
 * @author oyzh
 * @since 2025-04-25
 */
public class ShellSftpClientUtil {

    public static ShellSftpClient newClient(ShellConnect connect) {
        return new ShellSftpClient(connect);
    }

}
