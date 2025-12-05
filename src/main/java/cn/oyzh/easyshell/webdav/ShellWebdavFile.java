package cn.oyzh.easyshell.webdav;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import com.github.sardine.DavResource;

import java.util.Date;

/**
 * smb文件
 *
 * @author oyzh
 * @since 2025-07-23
 */
public class ShellWebdavFile implements ShellFile {

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
    private DavResource resource;

    /**
     * 最后修改时间
     */
    private Date lastModified;

    /**
     * 文件大小
     */
    private Long fileSize;

    public ShellWebdavFile(String parentPath, DavResource resource) {
        this.resource = resource;
        this.parentPath = parentPath;
    }

    @Override
    public boolean isFile() {
        return !this.isDirectory();
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
        if (this.fileSize != null) {
            return this.fileSize;
        }
        return this.resource.getContentLength();
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
        String fileName = this.resource.getName();
        if (fileName == null) {
            fileName = this.resource.getDisplayName();
        }
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean isDirectory() {
        return this.resource.isDirectory();
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
        Date date = null;
        if (this.lastModified != null) {
            date = this.lastModified;
        } else if (this.resource != null) {
            date = this.resource.getModified();
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
        Date date = this.resource.getCreation();
        if (date == null) {
            return "-";
        }
        return DateHelper.formatDateTime(date);
    }

    @Override
    public void copy(ShellFile t1) {
        if (t1 instanceof ShellWebdavFile file) {
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
    public void destroy() {
        NodeDestroyUtil.destroyObject(this.icon);
        this.icon = null;
    }
}
