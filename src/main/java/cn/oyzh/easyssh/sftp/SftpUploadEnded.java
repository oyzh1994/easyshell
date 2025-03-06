package cn.oyzh.easyssh.sftp;

import lombok.Data;

@Data
public class SftpUploadEnded {

    private int fileCount;

    private String fileName;
}
