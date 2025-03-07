package cn.oyzh.easyssh.sftp.download;

import lombok.Data;

/**
 * @author oyzh
 * @since 2025-03-07
 */
@Data
public class SftpDownloadChanged {

    private long total;

    private String remote;

    private long current;

    private long fileSize;

    private int fileCount;

    private String fileName;

    public double progress() {
        return 1D * this.current / this.total;
    }
}
