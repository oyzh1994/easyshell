package cn.oyzh.easyssh.sftp.download;

import lombok.Data;

/**
 * @author oyzh
 * @since 2025-03-07
 */
@Data
public class SftpDownloadCanceled {

    private String remoteFile;

    private int fileCount;

    private String localFileName;
}
