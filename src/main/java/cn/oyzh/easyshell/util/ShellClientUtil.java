package cn.oyzh.easyshell.util;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.ftp.ShellFTPClient;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.local.ShellLocalClient;
import cn.oyzh.easyshell.rlogin.ShellRLoginClient;
import cn.oyzh.easyshell.s3.ShellS3Client;
import cn.oyzh.easyshell.serial.ShellSerialClient;
import cn.oyzh.easyshell.sftp2.ShellSFTPClient;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.telnet.ShellTelnetClient;
import cn.oyzh.easyshell.vnc.ShellVNCClient;


/**
 * 客户端工具类
 *
 * @author oyzh
 * @since 2025-04-25
 */
public class ShellClientUtil {

    /**
     * 创建新客户端
     *
     * @param connect 连接
     * @param <T>     范型
     * @return 客户端
     */
    public static <T extends ShellBaseClient> T newClient(ShellConnect connect) {
        ShellBaseClient client = null;
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
        } else if (connect.isRloginType()) {
            client = new ShellRLoginClient(connect);
        } else if (connect.isVNCType()) {
            client = new ShellVNCClient(connect);
        } else if (connect.isLocalType()) {
            client = new ShellLocalClient(connect);
        } else if (connect.isS3Type()) {
            client = new ShellS3Client(connect);
        }
        return (T) client;
    }

}
