package cn.oyzh.easyssh.sftp;

import cn.oyzh.common.log.JulLog;
import com.jcraft.jsch.SftpProgressMonitor;
import lombok.Getter;

import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpMonitor implements SftpProgressMonitor {

    private final File file;

    @Getter
    private final String dest;

    private  final SftpUploadManager manager;

    public SftpMonitor(final File file,String dest,SftpUploadManager manager) {
        this.file = file;
        this.dest = dest;
        this.manager = manager;
    }

    @Override
    public void init(int i, String s, String s1, long l) {

    }

    @Override
    public boolean count(long l) {
        return true;
    }

    @Override
    public void end() {
        JulLog.info("file:{} upload finished", file.getPath());
        manager.remove(this);
    }

    public String getFilePath(){
        return file.getPath();
    }
}
