package cn.oyzh.easyshell.ftp;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import org.apache.commons.net.ftp.FTPFile;

import java.time.Instant;
import java.util.Calendar;

/**
 * ftp文件
 *
 * @author oyzh
 * @since 2025-04-26
 */
public class ShellFTPFile extends FTPFile implements ShellFile {

    /**
     * 父目录
     */
    private String parentPath;

    /**
     * 链接属性
     */
    private FTPFile file;

    public void setIcon(SVGGlyph icon) {
        this.icon = icon;
    }

    public ShellFTPFile(String parentPath, FTPFile file) {
        this.file = file;
        this.parentPath = parentPath;
    }

    private SVGGlyph icon;

    @Override
    public SVGGlyph getIcon() {
        if (this.icon == null) {
            this.icon = ShellFile.super.getIcon();
        }
        return this.icon;
    }

    @Override
    public boolean isLink() {
        return file.isSymbolicLink();
    }

    @Override
    public String getFileName() {
        return file.getName();
    }

    @Override
    public void copy(ShellFile t1) {
        if (t1 instanceof ShellFTPFile file) {
            this.file = file.file;
            this.parentPath = file.parentPath;
        }
    }

    @Override
    public boolean hasPermission(int access, int permission) {
        return file.hasPermission(access, permission);
    }

    @Override
    public String getUser() {
        return file.getUser();
    }

    @Override
    public int getType() {
        return file.getType();
    }

    @Override
    public Instant getTimestampInstant() {
        return file.getTimestampInstant();
    }

    @Override
    public Calendar getTimestamp() {
        return file.getTimestamp();
    }

    @Override
    public int getHardLinkCount() {
        return file.getHardLinkCount();
    }

    @Override
    public String getGroup() {
        return file.getGroup();
    }

    @Override
    public long getFileSize() {
        return this.file.getSize();
    }

    @Override
    public void setFileSize(long fileSize) {
        this.file.setSize(fileSize);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public String getParentPath() {
        return this.parentPath;
    }

    @Override
    public boolean isFile() {
        return file.isFile();
    }

    @Override
    public boolean isValid() {
        return file.isValid();
    }

    @Override
    public boolean isUnknown() {
        return file.isUnknown();
    }

    @Override
    public boolean isSymbolicLink() {
        return file.isSymbolicLink();
    }

    @Override
    public String getLink() {
        return file.getLink();
    }

    @Override
    public String getRawListing() {
        return file.getRawListing();
    }

    public void setFileName(String newName) {
        this.file.setName(newName);
    }

    @Override
    public String getModifyTime() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return "";
        }
        return DateHelper.formatDateTime(this.getTimestamp().getTime());
    }

    @Override
    public String getOwner() {
        return file.getUser();
    }

    @Override
    public String getPermissions() {
        return ShellFTPUtil.getPermissionsString(this.file);
    }

    @Override
    public void setPermission(int access, int permission, boolean value) {
        this.file.setPermission(access, permission, value);
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

}
