package cn.oyzh.easyssh.sftp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 */
@Data
public class SftpUploadChanged {

    private long total;

    private String dest;

    private long current;

    private long fileSize;

    private int fileCount;

    private String fileName;

    public double progress() {
        return 1D * this.current / this.total;
    }
}
