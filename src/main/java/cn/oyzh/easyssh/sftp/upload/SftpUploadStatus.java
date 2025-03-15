package cn.oyzh.easyssh.sftp.upload;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public enum SftpUploadStatus {

    IN_PREPARATION,
    UPLOADING,
    FINISHED,
    FAILED,
    CANCELED
}
