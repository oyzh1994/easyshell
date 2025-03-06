package cn.oyzh.easyssh.sftp;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpUploadManager {

    private Queue<SftpMonitor> monitors = new ArrayDeque<>();

    public void addFile(File file, String dest) {
        if (this.monitors == null) {
            this.monitors = new ArrayDeque<>();
        }
        this.monitors.add(new SftpMonitor(file, dest, this));
    }

    public SftpMonitor takeMonitor() {
        return this.monitors.peek();
    }

    public void remove(SftpMonitor monitor) {
        this.monitors.remove(monitor);
    }

    public boolean isEmpty() {
        return !this.monitors.isEmpty();
    }

    private final AtomicBoolean uploading = new AtomicBoolean(false);

    public  void setUploading(boolean uploading) {
        this.uploading.set(uploading);
    }

    public boolean isUploading() {
        return this.uploading.get();
    }
}
