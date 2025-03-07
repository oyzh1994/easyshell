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
    private Consumer<SftpUploadEnded> uploadEndedCallback;

    @Setter
    private Consumer<SftpUploadChanged> uploadChangedCallback;

    @Setter
    private Consumer<SftpUploadCanceled> uploadCanceledCallback;

    public void addFile(File file, String dest) {
        if (this.monitors == null) {
            this.monitors = new ArrayDeque<>();
        }
        this.monitors.add(new SftpUploadMonitor(file, dest, this));
    }

    public SftpUploadMonitor takeMonitor() {
        return this.monitors.peek();
    }

    public void uploadEnded(SftpUploadMonitor monitor) {
        this.monitors.remove(monitor);
        if (this.uploadEndedCallback != null) {
            SftpUploadEnded ended = new SftpUploadEnded();
            ended.setFileCount(this.size());
            ended.setDest(monitor.getDest());
            ended.setFileName(monitor.getFileName());
            this.uploadEndedCallback.accept(ended);
        }
    }

    public void uploadCanceled(SftpUploadMonitor monitor) {
        this.monitors.remove(monitor);
        if (this.uploadCanceledCallback != null) {
            SftpUploadCanceled ended = new SftpUploadCanceled();
            ended.setFileCount(this.size());
            ended.setDest(monitor.getDest());
            ended.setFileName(monitor.getFileName());
            this.uploadCanceledCallback.accept(ended);
        }
    }

    public void uploadChanged(SftpUploadMonitor monitor) {
        if (this.uploadChangedCallback != null) {
            SftpUploadChanged changed = new SftpUploadChanged();
            changed.setFileCount(this.size());
            changed.setFileSize(this.count());
            changed.setDest(monitor.getDest());
            changed.setTotal(monitor.getTotal());
            changed.setCurrent(monitor.getCurrent());
            changed.setFileName(monitor.getFileName());
            this.uploadChangedCallback.accept(changed);
        }
    }

    public boolean isEmpty() {
        return this.monitors.isEmpty();
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

    public void cancel() {
        for (SftpUploadMonitor monitor : this.monitors) {
            monitor.cancel();
        }
    }
}
