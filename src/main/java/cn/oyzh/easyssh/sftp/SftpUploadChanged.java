package cn.oyzh.easyssh.sftp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 */
@Data
public class SftpUploadChanged {

    private long total;

    private long current;

    private long fileSize;

    private String fileName;

    private int fileCount;

    public SftpUploadChanged() {
    }

    public SftpUploadChanged(long total, long current, String fileName) {
        this.total = total;
        this.current = current;
        this.fileName = fileName;
    }

}
