package cn.oyzh.easyssh.sftp;

import cn.oyzh.common.log.JulLog;
import com.jcraft.jsch.SftpProgressMonitor;
import lombok.Getter;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpUploadMonitor implements SftpProgressMonitor {

    private long total;

    private long current;

    private final File file;

    @Getter
    private final String dest;

    private final SftpUploadManager manager;

    public SftpUploadMonitor(final File file, String dest, SftpUploadManager manager) {
        this.file = file;
        this.dest = dest;
        this.manager = manager;
    }

    @Override
    public void init(int current, String s, String s1, long total) {
        this.total = total;
    }

    @Override
    public boolean count(long current) {
        this.current += current;
        this.manager.uploadChanged(new SftpUploadChanged(this.total, this.current, this.getFileName()));
        return true;
    }

    @Override
    public void end() {
        JulLog.info("file:{} upload finished", this.getFilePath());
        this.manager.uploadEnd(this);
    }

    public String getFileName() {
        return this.file.getName();
    }

    public String getFilePath() {
        return this.file.getPath();
    }
}
