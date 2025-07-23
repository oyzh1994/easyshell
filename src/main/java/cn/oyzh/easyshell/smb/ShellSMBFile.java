package cn.oyzh.easyshell.smb;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.easyshell.file.ShellFile;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;

import java.time.Instant;
import java.util.Date;

/**
 * smb文件
 *
 * @author oyzh
 * @since 2025-07-23
 */
public class ShellSMBFile implements ShellFile {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 父路径
     */
    private String parentPath;

    /**
     * 文件对象
     */
    private final FileIdBothDirectoryInformation information;

    /**
     * 最后修改时间
     */
    private Instant lastModified;

    /**
     * 文件大小
     */
    private Long fileSize;

    public ShellSMBFile(FileIdBothDirectoryInformation information) {
        this.information = information;
        this.fileName = this.information.getFileName();
    }

    @Override
    public boolean isFile() {
        long val = this.information.getFileAttributes() & FileAttributes.FILE_ATTRIBUTE_SPARSE_FILE.getValue();
        return val != 0;
    }

    @Override
    public boolean isLink() {
        return false;
    }

    @Override
    public String getOwner() {
        return "-";
    }

    @Override
    public String getGroup() {
        return "-";
    }

    @Override
    public long getFileSize() {
        return this.information.getEaSize();
    }

    @Override
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String getFileName() {
        return this.information.getFileName();
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean isDirectory() {
        long val = this.information.getFileAttributes() & FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue();
        return val != 0;
    }

    @Override
    public String getParentPath() {
        return this.parentPath;
    }

    @Override
    public String getPermissions() {
        return "-";
    }

    @Override
    public void setPermissions(String permissions) {

    }

    @Override
    public String getModifyTime() {
        Instant instant = null;
        if (this.lastModified != null) {
            instant = this.lastModified;
        } else if (this.information != null) {
            instant = this.information.getChangeTime().toInstant();
        }
        if (instant != null) {
            Date date = new Date(instant.toEpochMilli());
            return DateHelper.formatDateTime(date);
        }
        return "-";
    }

    @Override
    public void setModifyTime(String modifyTime) {
    }

    public String getAddTime() {
        Instant instant = this.information.getCreationTime().toInstant();
        Date date = new Date(instant.toEpochMilli());
        return DateHelper.formatDateTime(date);
    }

    @Override
    public void copy(ShellFile t1) {
        if (t1 instanceof ShellSMBFile file) {
            if (file.lastModified != null) {
                this.lastModified = file.lastModified;
            }
            if (file.fileSize != null) {
                this.fileSize = file.fileSize;
            }
            this.fileName = file.fileName;
            this.parentPath = file.parentPath;
        }
    }

    @Override
    public String getFilePath() {
        return this.information.getFileName();
    }

}
