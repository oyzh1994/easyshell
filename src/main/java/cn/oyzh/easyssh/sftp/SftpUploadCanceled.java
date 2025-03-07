package cn.oyzh.easyssh.sftp;

import lombok.Data;

@Data
public class SftpUploadCanceled {

    private String dest;

    private int fileCount;

    private String fileName;
}
