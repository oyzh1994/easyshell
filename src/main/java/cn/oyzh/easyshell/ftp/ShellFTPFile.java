package cn.oyzh.easyshell.ftp;

import cn.oyzh.common.date.CalendarUtil;
import cn.oyzh.common.date.DateHelper;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import org.apache.commons.net.ftp.FTPFile;

import java.util.Calendar;
import java.util.Date;

/**
 * ftp文件
 *
 * @author oyzh
 * @since 2025-04-26
 */
public class ShellFTPFile implements ShellFile {

    /**
     * ftp文件
     */
    private FTPFile file;

    /**
     * ftp链接文件
     */
    private FTPFile linkFile;

    /**
     * 文件图标
     */
    private SVGGlyph icon;

    /**
     * 父路径
     */
    private String parentPath;

    public ShellFTPFile(String parentPath, FTPFile file, FTPFile linkFile) {
        this.file = file;
        this.linkFile = linkFile;
        this.parentPath = parentPath;
    }

    @Override
    public SVGGlyph getIcon() {
        if (this.icon == null) {
            this.icon = ShellFile.super.getIcon();
        }
        return this.icon;
    }

    @Override
    public boolean isLink() {
        return this.file.isSymbolicLink();
    }

    @Override
    public String getFileName() {
        return this.file.getName();
    }

    @Override
    public void copy(ShellFile t1) {
        if (t1 instanceof ShellFTPFile f1) {
            this.file = f1.file;
            this.linkFile = f1.linkFile;
            this.parentPath = f1.parentPath;
        }
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
    public boolean isDirectory() {
        if (this.isLink()) {
            if (this.linkFile != null) {
                return this.linkFile.isDirectory();
            }
            return false;
        }
        return this.file.isDirectory();
    }

    @Override
    public String getParentPath() {
        return this.parentPath;
    }

    @Override
    public boolean isFile() {
        if (this.isLink()) {
            if (this.linkFile != null) {
                return this.linkFile.isFile();
            }
            return false;
        }
        return this.file.isFile();
    }

    @Override
    public void setFileName(String newName) {
        this.file.setName(newName);
    }

    @Override
    public String getModifyTime() {
        if (!this.isNormal()) {
            return "";
        }
        return DateHelper.formatDateTime(this.file.getTimestamp().getTime());
    }

    @Override
    public void setModifyTime(String modifyTime) {
        Date date = DateHelper.parseDateTime(modifyTime);
        Calendar calendar = CalendarUtil.of(date);
        this.file.setTimestamp(calendar);
    }

    @Override
    public String getOwner() {
        return this.file.getUser();
    }

    @Override
    public String getPermissions() {
        return ShellFTPUtil.getPermissionsString(this.file);
    }

    @Override
    public void setPermissions(String permissions) {
        if (permissions.length() == 10) {
            permissions = permissions.substring(1, 10);
        }

        this.file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, permissions.charAt(0) == 'r');
        this.file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, permissions.charAt(1) == 'w');
        this.file.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, permissions.charAt(2) == 'x');

        this.file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION, permissions.charAt(3) == 'r');
        this.file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION, permissions.charAt(4) == 'w');
        this.file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION, permissions.charAt(5) == 'x');

        this.file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, permissions.charAt(6) == 'r');
        this.file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION, permissions.charAt(7) == 'w');
        this.file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION, permissions.charAt(8) == 'x');
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

//    public boolean hasPermission(int access, int permission) {
//        return file.hasPermission(access, permission);
//    }
//
//    public void setPermission(int access, int permission, boolean value) {
//        this.file.setPermission(access, permission, value);
//    }

}
