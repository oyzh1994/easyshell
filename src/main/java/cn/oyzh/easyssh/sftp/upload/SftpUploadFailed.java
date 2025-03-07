package cn.oyzh.easyssh.sftp.upload;

import lombok.Data;

/**
 * @author oyzh
 * @since 2025-03-07
 */
@Data
public class SftpUploadFailed {

    private String dest;

    private int fileCount;

    private String fileName;
}
