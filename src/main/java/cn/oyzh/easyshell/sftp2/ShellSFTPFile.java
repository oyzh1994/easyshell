package cn.oyzh.easyshell.sftp2;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellSFTPFile implements ShellFile {

    /**
     * 文件对象
     */
    private ChannelSftp.LsEntry entry;

    /**
     * 文件属性
     */
    private SftpATTRS attrs;

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
    private SftpATTRS linkAttrs;

    /**
     * 文件图标
     */
    private SVGGlyph icon;

    public ChannelSftp.LsEntry getEntry() {
        return entry;
    }

    public void setEntry(ChannelSftp.LsEntry entry) {
        this.entry = entry;
    }

    public SftpATTRS getAttrs() {
        if (this.attrs == null) {
            return this.entry.getAttrs();
        }
        return this.attrs;
    }

    public SftpATTRS getLinkAttrs() {
        return linkAttrs;
    }

    public void setLinkAttrs(SftpATTRS linkAttrs) {
        this.linkAttrs = linkAttrs;
        if (this.icon != null) {
            this.refreshIcon();
        }
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public long getFileSize() {
        return this.getAttrs().getSize();
    }

    @Override
    public void setFileSize(long fileSize) {
        this.getAttrs().setSIZE(fileSize);
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public void setIcon(SVGGlyph icon) {
        this.icon = icon;
    }

    public ShellSFTPFile(String parentPath, ChannelSftp.LsEntry entry) {
        this.parentPath = parentPath;
        this.entry = entry;
        String[] arr = entry.getLongname().split("\\s+");
        if (arr.length > 2) {
            this.owner = arr[2];
        }
        if (arr.length > 3) {
            this.group = arr[3];
        }
        this.updatePermissions();
    }

    public ShellSFTPFile(String parentPath, String fileName, SftpATTRS attrs) {
        this.parentPath = parentPath;
        this.fileName = fileName;
        this.attrs = attrs;
        this.updatePermissions();
    }

    @Override
    public SVGGlyph getIcon() {
        if (this.icon == null) {
            this.refreshIcon();
        }
        return this.icon;
    }

    /**
     * 刷新图标
     */
    private void refreshIcon() {
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
        this.permissionsProperty().set(this.getAttrs().getPermissionsString());
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
        int permissionInt = ShellFileUtil.toPermissionInt(permissions);
        this.getAttrs().setPERMISSIONS(permissionInt);
        this.updatePermissions();
    }

    public String getAddTime() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        int aTime = this.getAttrs().getATime();
        return DateHelper.formatDateTime(new Date(aTime * 1000L));
    }

    @Override
    public String getModifyTime() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        int mtime = this.getAttrs().getMTime();
        return DateHelper.formatDateTime(new Date(mtime * 1000L));
    }

    /**
     * 获取修改时间戳
     *
     * @return 结果
     */
    public int getMTime() {
        return this.getAttrs().getMTime();
    }

    @Override
    public void setModifyTime(String modifyTime) {
        Date date = DateHelper.parseDateTime(modifyTime);
        int atime = this.getAttrs().getATime();
        int mtime = Math.toIntExact(date.getTime() / 1000);
        this.getAttrs().setACMODTIME(atime, mtime);
    }

    public int getUid() {
        return this.getAttrs().getUId();
    }

    public int getGid() {
        return this.getAttrs().getGId();
    }

    @Override
    public boolean isFile() {
        if (this.isLink()) {
            if (this.linkAttrs != null) {
                return this.linkAttrs.isReg();
            }
            return false;
        }
        return this.getAttrs().isReg();
    }

    @Override
    public boolean isLink() {
        return this.getAttrs().isLink();
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
//            this.linkPath = file.linkPath;
            this.linkAttrs = file.linkAttrs;
            this.parentPath = file.parentPath;
            this.updatePermissions();
        }
    }

    @Override
    public boolean isDirectory() {
        if (this.isLink()) {
            if (this.linkAttrs != null) {
                return this.linkAttrs.isDir();
            }
            return false;
        }
        return this.getAttrs().isDir();
    }

//    /**
//     * 设置链接文件
//     *
//     * @param linkPath 链接文件
//     */
//    public void setLinkPath(String linkPath) {
//        this.linkPath = linkPath;
//    }
//
//    /**
//     * 获取链接路径
//     *
//     * @return 链接路径
//     */
//    public String getLinkPath() {
//        return linkPath;
//    }
}
