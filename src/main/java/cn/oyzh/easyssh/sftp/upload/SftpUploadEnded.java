package cn.oyzh.easyssh.sftp.upload;

import lombok.Data;

/**
 * @author oyzh
 * @since 2025-03-07
 */
@Data
public class SftpUploadEnded {

    private String dest;

    private int fileCount;

    private String fileName;
}
