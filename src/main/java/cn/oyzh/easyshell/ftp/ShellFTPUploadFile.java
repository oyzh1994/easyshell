package cn.oyzh.easyshell.ftp;

import cn.oyzh.common.util.NumberUtil;

public class ShellFTPUploadFile {

    private long size;

    private Thread task;

    private String localPath;

    private String remotePath;

    public String getLocalPath() {
        return localPath;
    }

    public Thread getTask() {
        return task;
    }

    public void setTask(Thread task) {
        this.task = task;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getFileSize(){
        return NumberUtil.formatSize(this.size,2);
    }
}
