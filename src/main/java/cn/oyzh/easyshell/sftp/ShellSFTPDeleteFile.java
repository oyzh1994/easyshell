package cn.oyzh.easyshell.sftp;

/**
 * @author oyzh
 * @since 2025-04-28
 */
public class ShellSFTPDeleteFile {

    private Thread task;

    private ShellSFTPFile file;

    public Thread getTask() {
        return task;
    }

    public void setTask(Thread task) {
        this.task = task;
    }

    public ShellSFTPFile getFile() {
        return file;
    }

    public void setFile(ShellSFTPFile file) {
        this.file = file;
    }
}
