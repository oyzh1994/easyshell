package cn.oyzh.easyshell.event.sftp;


import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/3/29
 */
public class ShellSftpFileSavedEvent extends Event<ShellSFTPFile> {

    public String fileName() {
        return this.data().getFileName();
    }
}
