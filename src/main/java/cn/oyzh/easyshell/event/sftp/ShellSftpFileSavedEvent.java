package cn.oyzh.easyshell.event.sftp;


import cn.oyzh.easyshell.sftp.ShellSftpFile;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/3/29
 */
public class ShellSftpFileSavedEvent extends Event<ShellSftpFile> {

    public String fileName() {
        return this.data().getFileName();
    }
}
