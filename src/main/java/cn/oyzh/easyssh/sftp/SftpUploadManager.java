package cn.oyzh.easyssh.sftp;

import lombok.Setter;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpUploadManager {

    private Queue<SftpUploadMonitor> monitors = new ArrayDeque<>();

    @Setter
    private Consumer<SftpUploadEnded> uploadEndCallback;

    @Setter
    private Consumer<SftpUploadChanged> uploadChangedCallback;

    public void addFile(File file, String dest) {
        if (this.monitors == null) {
            this.monitors = new ArrayDeque<>();
        }
        this.monitors.add(new SftpUploadMonitor(file, dest, this));
    }

    public SftpUploadMonitor takeMonitor() {
        return this.monitors.peek();
    }

    public void uploadEnd(SftpUploadMonitor monitor) {
        this.monitors.remove(monitor);
        if (this.uploadEndCallback != null) {
            SftpUploadEnded ended = new SftpUploadEnded();
            ended.setFileCount(this.size());
            ended.setFileName(monitor.getFileName());
            this.uploadEndCallback.accept(ended);
        }
    }

    public void uploadChanged(SftpUploadMonitor monitor) {
        if (this.uploadChangedCallback != null) {
            SftpUploadChanged changed = new SftpUploadChanged();
            changed.setFileCount(this.size());
            changed.setFileSize(this.count());
            changed.setTotal(monitor.getTotal());
            changed.setCurrent(monitor.getCurrent());
            changed.setFileName(monitor.getFileName());
            this.uploadChangedCallback.accept(changed);
        }
    }

    public boolean isEmpty() {
        return this.monitors.isEmpty();
    }

    private final AtomicBoolean uploading = new AtomicBoolean(false);

    public void setUploading(boolean uploading) {
        this.uploading.set(uploading);
    }

    public boolean isUploading() {
        return this.uploading.get();
    }

    public int size() {
        return this.monitors.size();
    }

    public long count() {
        long cnt = 0;
        for (SftpUploadMonitor monitor : this.monitors) {
            cnt += monitor.getFileLength();
        }
        return cnt;
    }
}
