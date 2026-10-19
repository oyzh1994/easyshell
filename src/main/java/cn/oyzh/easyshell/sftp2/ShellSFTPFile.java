package cn.oyzh.easyshell.sftp2;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.sshd.sftp.client.SftpClient;

import java.nio.file.attribute.FileTime;
import java.util.Date;

/**
 * sftp文件
 *
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

    /**
     * 链接属性
     */
    private SftpClient.Attributes linkAttrs;

    /**
     * 获取文件对象
     *
     * @return 文件对象
     */
    public SftpClient.DirEntry getEntry() {
        return entry;
    }

    /**
     * 设置文件对象
     *
     * @param entry 文件对象
     */
    public void setEntry(SftpClient.DirEntry entry) {
        this.entry = entry;
    }

    /**
     * 获取文件属性
     *
     * @return 文件属性
     */
    public SftpClient.Attributes getAttrs() {
        if (this.attrs == null) {
            return this.entry.getAttributes();
        }
        return this.attrs;
    }

    /**
     * 获取链接属性
     *
     * @return 链接属性
     */
    public SftpClient.Attributes getLinkAttrs() {
        return linkAttrs;
    }

    /**
     * 设置链接属性
     *
     * @param linkAttrs 链接属性
     */
    public void setLinkAttrs(SftpClient.Attributes linkAttrs) {
        this.linkAttrs = linkAttrs;
    }

    @Override
    public String getOwner() {
        if (this.owner == null) {
            return this.getAttrs().getOwner();
        }
        return owner;
    }

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

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getParentPath() {
        return parentPath;
    }

    /**
     * 设置父路径
     *
     * @param parentPath 父路径
     */
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    /**
     * 构造sftp文件
     *
     * @param parentPath 父路径
     * @param entry      文件对象
     */
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

    /**
     * 构造sftp文件
     *
     * @param parentPath 父路径
     * @param fileName   文件名
     * @param attrs      文件属性
     */
    public ShellSFTPFile(String parentPath, String fileName, SftpClient.Attributes attrs) {
        this.parentPath = parentPath;
        this.fileName = fileName;
        this.attrs = attrs;
        this.updatePermissions();
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

    /**
     * 权限属性
     */
    private StringProperty permissionsProperty;

    /**
     * 获取权限属性
     *
     * @return 权限属性
     */
    public StringProperty permissionsProperty() {
        if (this.permissionsProperty == null) {
            this.permissionsProperty = new SimpleStringProperty();
        }
        return this.permissionsProperty;
    }

    /**
     * 更新权限
     */
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

    /**
     * 获取访问时间
     *
     * @return 访问时间
     */
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
        if (date != null) {
            FileTime mtime = FileTime.fromMillis(date.getTime());
            this.getAttrs().setModifyTime(mtime);
        }
    }

    /**
     * 获取拥有者id
     *
     * @return 拥有者id
     */
    public int getUid() {
        return this.getAttrs().getUserId();
    }

    /**
     * 获取分组id
     *
     * @return 分组id
     */
    public int getGid() {
        return this.getAttrs().getGroupId();
    }

    @Override
    public boolean isFile() {
        if (this.isLink() && this.linkAttrs != null) {
            return this.linkAttrs.isRegularFile();
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
        if (this.isLink() && this.linkAttrs != null) {
            return this.linkAttrs.isDirectory();
        }
        return this.getAttrs().isDirectory();
    }

    @Override
    public void destroy() {
        if (this.permissionsProperty != null) {
            this.permissionsProperty.unbind();
            this.permissionsProperty = null;
        }
        this.attrs = null;
        this.owner = null;
        this.group = null;
        this.entry = null;
        this.fileName = null;
        this.linkAttrs = null;
        this.parentPath = null;
    }
}
