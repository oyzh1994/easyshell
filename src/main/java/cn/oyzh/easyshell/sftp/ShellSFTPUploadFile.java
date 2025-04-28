package cn.oyzh.easyshell.sftp;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-04-28
 */
public class ShellSFTPUploadFile {

    private Thread task;

    private File localFile;

    private String remotePath;

    public Thread getTask() {
        return task;
    }

    public void setTask(Thread task) {
        this.task = task;
    }

    public File getLocalFile() {
        return localFile;
    }

    public void setLocalFile(File localFile) {
        this.localFile = localFile;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }
}
