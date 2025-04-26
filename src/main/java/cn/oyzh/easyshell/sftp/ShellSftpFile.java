package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.util.ShellFile;
import cn.oyzh.easyshell.util.ShellFileUtil;
import cn.oyzh.easyshell.util.ShellUtil;
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
public class ShellSftpFile implements ObjectCopier<ShellSftpFile>, ShellFile {

    private ChannelSftp.LsEntry entry;

    private SftpATTRS attrs;

    private String owner;

    private String group;

    private String fileName;

    private String parentPath;

    /**
     * 链接路径
     */
    private String linkPath;

    /**
     * 链接属性
     */
    private SftpATTRS linkAttrs;

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
        return attrs;
    }

    public void setAttrs(SftpATTRS attrs) {
        this.attrs = attrs;
        this.updatePermissions();
    }

    public SftpATTRS getLinkAttrs() {
        return linkAttrs;
    }

    public void setLinkAttrs(SftpATTRS linkAttrs) {
        this.linkAttrs = linkAttrs;
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
        return this.size();
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


    public ShellSftpFile(String parentPath, ChannelSftp.LsEntry entry) {
        this.parentPath = parentPath;
        this.entry = entry;
        this.updatePermissions();
    }

    public ShellSftpFile(String parentPath, String fileName, SftpATTRS attrs) {
        this.parentPath = parentPath;
        this.fileName = fileName;
        this.attrs = attrs;
        this.updatePermissions();
    }

    public boolean isNormal() {
        return !this.isCurrentFile() && !this.isReturnDirectory();
    }

    private SVGGlyph icon;

    public SVGGlyph getIcon() {
        if (this.icon == null) {
            this.icon = ShellFile.super.getIcon();
        }
        return this.icon;
    }

    public String getSize() {
        if (this.isDir() || this.isReturnDirectory() || this.isCurrentFile()) {
            return "-";
        }
        return NumberUtil.formatSize(this.getAttrs().getSize(), 4);
    }

    public long size() {
        return this.getAttrs().getSize();
    }

    public String getName() {
        String fileName = this.getFileName();
        if (fileName.contains("/")) {
            return fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        return fileName;
    }

    public String getFileName() {
        if (this.fileName == null) {
            return this.entry.getFilename();
        }
        return this.fileName;
    }

    public String getFilePath() {
        if (this.linkPath != null) {
            return this.linkPath;
        }
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

    public String getPermissions() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        return permissionsProperty().get();
    }

    public String getAddTime() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        int aTime = this.getAttrs().getATime();
        return DateHelper.formatDateTime(new Date(aTime * 1000L));
    }

    public String getModifyTime() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        int mtime = this.getAttrs().getMTime();
        return DateHelper.formatDateTime(new Date(mtime * 1000L));
    }

    public int getUid() {
        return this.getAttrs().getUId();
    }

    public int getGid() {
        return this.getAttrs().getGId();
    }

    public boolean isHiddenFile() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return false;
        }
        return this.getFileName().startsWith(".");
    }

    public boolean isCurrentFile() {
        return ".".equals(this.getName());
    }

    public boolean isReturnDirectory() {
        return "..".equals(this.getName());
    }

    public int getOrder() {
        if (this.isReturnDirectory()) {
            return -10;
        }
        if (this.isDir()) {
            if (this.isHiddenFile()) {
                return -9;
            }
            return -8;
        }
        if (this.isHiddenFile()) {
            return -7;
        }
        return 0;
    }

    public boolean isDir() {
        if (this.isLink()) {
            if (this.linkAttrs != null) {
                return this.linkAttrs.isDir();
            }
            return false;
        }
        return this.getAttrs().isDir();
    }

    public boolean isFile() {
        if (this.isLink()) {
            if (this.linkAttrs != null) {
                return this.linkAttrs.isReg();
            }
            return false;
        }
        return this.getAttrs().isReg();
    }

    public boolean isLink() {
        return this.getAttrs().isLink();
    }

    @Override
    public void copy(ShellSftpFile t1) {
        if (t1.entry != null) {
            this.entry = t1.entry;
        }
        if (t1.attrs != null) {
            this.attrs = t1.attrs;
        }
        if (t1.owner != null) {
            this.owner = t1.owner;
        }
        if (t1.group != null) {
            this.group = t1.group;
        }
        this.fileName = t1.fileName;
        this.linkPath = t1.linkPath;
        this.linkAttrs = t1.linkAttrs;
        this.parentPath = t1.parentPath;
    }

    public boolean canWrite() {
        return this.getPermissions().contains("w");
    }

    public boolean isDirectory() {
        return this.isDir();
    }

    public String getPath() {
        return this.getFilePath();
    }

    public long length() {
        return this.size();
    }

    public boolean hasOwnerReadPermission() {
        return ShellUtil.hasOwnerReadPermission(this.getPermissions());
    }

    public boolean hasOwnerWritePermission() {
        return ShellUtil.hasOwnerWritePermission(this.getPermissions());
    }

    public boolean hasOwnerExecutePermission() {
        return ShellUtil.hasOwnerExecutePermission(this.getPermissions());
    }

    public boolean hasGroupsReadPermission() {
        return ShellUtil.hasGroupsReadPermission(this.getPermissions());
    }

    public boolean hasGroupsWritePermission() {
        return ShellUtil.hasGroupsWritePermission(this.getPermissions());
    }

    public boolean hasGroupsExecutePermission() {
        return ShellUtil.hasGroupsExecutePermission(this.getPermissions());
    }

    public boolean hasOthersReadPermission() {
        return ShellUtil.hasOthersReadPermission(this.getPermissions());
    }

    public boolean hasOthersWritePermission() {
        return ShellUtil.hasOthersWritePermission(this.getPermissions());
    }

    public String getLinkPath() {
        return linkPath;
    }

    public void setLinkPath(String linkPath) {
        this.linkPath = linkPath;
    }

    public boolean hasOthersExecutePermission() {
        return ShellUtil.hasOthersExecutePermission(this.getPermissions());
    }

    /**
     * 是否根目录
     *
     * @return 结果
     */
    public boolean isRoot() {
        return this.isDir() && "/".equals(this.getFilePath());
    }
}
