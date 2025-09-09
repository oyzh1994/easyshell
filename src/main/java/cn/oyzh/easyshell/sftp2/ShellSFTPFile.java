package cn.oyzh.easyshell.sftp2;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.sshd.sftp.client.SftpClient;

import java.nio.file.attribute.FileTime;
import java.util.Date;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellSFTPFile implements ShellFile {

    /**
     * 文件对象
     */
    private SftpClient.DirEntry entry;

    /**
     * 文件属性
     */
    private SftpClient.Attributes attrs;

    /**
     * 拥有者
     */
    private String owner;

    /**
     * 分组
     */
    private String group;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 父路径
     */
    private String parentPath;

//    /**
//     * 链接路径
//     */
//    private String linkPath;

    /**
     * 链接属性
     */
    private SftpClient.Attributes linkAttrs;

    public SftpClient.DirEntry getEntry() {
        return entry;
    }

    public void setEntry(SftpClient.DirEntry entry) {
        this.entry = entry;
    }

    public SftpClient.Attributes getAttrs() {
        if (this.attrs == null) {
            return this.entry.getAttributes();
        }
        return this.attrs;
    }

    public SftpClient.Attributes getLinkAttrs() {
        return linkAttrs;
    }

    public void setLinkAttrs(SftpClient.Attributes linkAttrs) {
        this.linkAttrs = linkAttrs;
        if (this.icon != null) {
            this.refreshIcon();
        }
    }

    @Override
    public String getOwner() {
        if (this.owner == null) {
            return this.getAttrs().getOwner();
        }
        return owner;
    }

    // public void setOwner(String owner) {
    //     this.owner = owner;
    // }

    @Override
    public String getGroup() {
        if (this.group == null) {
            return this.getAttrs().getGroup();
        }
        return group;
    }

    @Override
    public long getFileSize() {
        return this.getAttrs().getSize();
    }

    @Override
    public void setFileSize(long fileSize) {
        this.getAttrs().setSize(fileSize);
    }

    // public void setGroup(String group) {
    //     this.group = group;
    // }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public ShellSFTPFile(String parentPath, SftpClient.DirEntry entry) {
        this.parentPath = parentPath;
        this.entry = entry;
        String[] arr = entry.getLongFilename().split("\\s+");
        if (arr.length > 2) {
            this.owner = arr[2];
        }
        if (arr.length > 3) {
            this.group = arr[3];
        }
        this.updatePermissions();
    }

    public ShellSFTPFile(String parentPath, String fileName, SftpClient.Attributes attrs) {
        this.parentPath = parentPath;
        this.fileName = fileName;
        this.attrs = attrs;
        this.updatePermissions();
    }

    /**
     * 文件图标
     */
    private SVGGlyph icon;

    @Override
    public SVGGlyph getIcon() {
        if (this.icon == null) {
            this.refreshIcon();
        }
        return this.icon;
    }

    @Override
    public void refreshIcon() {
        this.icon = ShellFile.super.getIcon();
    }

    @Override
    public String getFileName() {
        if (this.fileName == null) {
            return this.entry.getFilename();
        }
        return this.fileName;
    }

    @Override
    public String getFilePath() {
        String fileName = this.getFileName();
        if (fileName.startsWith("/")) {
            return fileName;
        }
        return ShellFileUtil.concat(this.parentPath, fileName);
    }

    private StringProperty permissionsProperty;

    public StringProperty permissionsProperty() {
        if (this.permissionsProperty == null) {
            this.permissionsProperty = new SimpleStringProperty();
        }
        return this.permissionsProperty;
    }

    protected void updatePermissions() {
        String permissions = ShellSFTPUtil.formatPermissions(this.getAttrs());
        this.permissionsProperty().set(permissions);
    }

    @Override
    public String getPermissions() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        return permissionsProperty().get();
    }

    @Override
    public void setPermissions(String permissions) {
        permissions = ShellSFTPUtil.getFileType(this.getAttrs()) + permissions;
        this.permissionsProperty().set(permissions);
    }

    public String getAddTime() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        FileTime aTime = this.getAttrs().getAccessTime();
        return DateHelper.formatDateTime(aTime.toInstant());
    }

    @Override
    public String getModifyTime() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        FileTime mtime = this.getAttrs().getModifyTime();
        return DateHelper.formatDateTime(mtime.toInstant());
    }

    /**
     * 获取修改时间戳
     *
     * @return 结果
     */
    public long getMTime() {
        return this.getAttrs().getModifyTime().toMillis();
    }

    @Override
    public void setModifyTime(String modifyTime) {
        Date date = DateHelper.parseDateTime(modifyTime);
        FileTime mtime = FileTime.fromMillis(date.getTime());
        this.getAttrs().setModifyTime(mtime);
    }

    public int getUid() {
        return this.getAttrs().getUserId();
    }

    public int getGid() {
        return this.getAttrs().getGroupId();
    }

    @Override
    public boolean isFile() {
        if (this.isLink()) {
            if (this.linkAttrs != null) {
                return this.linkAttrs.isRegularFile();
            }
            return false;
        }
        return this.getAttrs().isRegularFile();
    }

    @Override
    public boolean isLink() {
        return this.getAttrs().isSymbolicLink();
    }

    @Override
    public void copy(ShellFile t1) {
        if (t1 instanceof ShellSFTPFile file) {
            if (file.entry != null) {
                this.entry = file.entry;
            }
            if (file.attrs != null) {
                this.attrs = file.attrs;
            }
            if (file.owner != null) {
                this.owner = file.owner;
            }
            if (file.group != null) {
                this.group = file.group;
            }
            this.fileName = file.fileName;
            this.linkAttrs = file.linkAttrs;
            this.parentPath = file.parentPath;
            this.updatePermissions();
        }
    }

    @Override
    public boolean isDirectory() {
        if (this.isLink()) {
            if (this.linkAttrs != null) {
                return this.linkAttrs.isDirectory();
            }
            return false;
        }
        return this.getAttrs().isDirectory();
    }
}
