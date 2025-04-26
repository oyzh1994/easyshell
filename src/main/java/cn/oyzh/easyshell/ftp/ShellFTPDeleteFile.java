package cn.oyzh.easyshell.ftp;

public class ShellFTPDeleteFile {

    private long size;

    private Thread task;

    private String remotePath;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public Thread getTask() {
        return task;
    }

    public void setTask(Thread task) {
        this.task = task;
    }
}
