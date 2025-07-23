package cn.oyzh.easyshell.smb;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileUtil;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;

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
     * 文件属性
     */
    private FileAllInformation allInformation;

    /**
     * 文件属性
     */
    private FileIdBothDirectoryInformation information;

    /**
     * 最后修改时间
     */
    private Date lastModified;

    /**
     * 文件大小
     */
    private Long fileSize;

    public ShellSMBFile(String parentPath, FileIdBothDirectoryInformation information) {
        this.information = information;
        this.parentPath = parentPath;
    }

    public ShellSMBFile(String filePath, FileAllInformation information) {
        this.allInformation = information;
        this.fileName = ShellFileUtil.name(filePath);
        this.parentPath = ShellFileUtil.parent(filePath);
    }

    /**
     * 文件属性
     *
     * @return 属性
     */
    private long attrs() {
        long attrs;
        if (this.allInformation != null) {
            attrs = this.allInformation.getBasicInformation().getFileAttributes() & FileAttributes.FILE_ATTRIBUTE_SPARSE_FILE.getValue();
        } else {
            attrs = this.information.getFileAttributes() & FileAttributes.FILE_ATTRIBUTE_SPARSE_FILE.getValue();
        }
        return attrs;
    }

    /**
     * 文件大小
     *
     * @return 文件大小
     */
    private long easize() {
        long easize;
        if (this.allInformation != null) {
            easize = this.allInformation.getEaInformation().getEaSize();
        } else {
            easize = this.information.getEaSize();
        }
        return easize;
    }

    @Override
    public boolean isFile() {
        long val = this.attrs() & FileAttributes.FILE_ATTRIBUTE_SPARSE_FILE.getValue();
        return val != 0;
    }

    @Override
    public boolean isLink() {
        long val = this.attrs() & FileAttributes.FILE_ATTRIBUTE_REPARSE_POINT.getValue();
        if (val == 0) {
            return false;
        }
        return (this.easize() & 0xA0000000L) == 0xA0000000L;
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
        if (this.fileSize != null) {
            return this.fileSize;
        }
        return this.easize();
    }

    @Override
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String getFileName() {
        if (this.fileName != null) {
            return this.fileName;
        }
        if (this.allInformation != null) {
            return this.allInformation.getNameInformation();
        }
        return this.information.getFileName();
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean isDirectory() {
        long val = this.attrs() & FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue();
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
        Date date;
        if (this.lastModified != null) {
            date = this.lastModified;
        } else if (this.allInformation != null) {
            date = this.allInformation.getBasicInformation().getChangeTime().toDate();
        } else {
            date = this.information.getChangeTime().toDate();
        }
        return DateHelper.formatDateTime(date);
    }

    @Override
    public void setModifyTime(String modifyTime) {
        this.lastModified = DateHelper.parseDateTime(modifyTime);
    }

    /**
     * 获取添加时间
     *
     * @return 添加时间
     */
    public String getAddTime() {
        Date date;
        if (this.allInformation != null) {
            date = this.allInformation.getBasicInformation().getCreationTime().toDate();
        } else {
            date = this.information.getCreationTime().toDate();
        }
        return DateHelper.formatDateTime(date);
    }

    @Override
    public void copy(ShellFile t1) {
        if (t1 instanceof ShellSMBFile file) {
            if (file.fileSize != null) {
                this.fileSize = file.fileSize;
            }
            if (file.lastModified != null) {
                this.lastModified = file.lastModified;
            }
            this.fileName = file.fileName;
            this.parentPath = file.parentPath;
        }
    }
}
