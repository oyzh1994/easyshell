package cn.oyzh.easyshell.file;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.fx.svg.glyph.ReturnFolderSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.FileSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.FolderLinkSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.FileLinkSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.FolderSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 文件接口
 *
 * @author oyzh
 * @since 2025-04-28
 */
public interface ShellFile extends ObjectCopier<ShellFile>, Destroyable {

    /**
     * 是否文件
     *
     * @return 结果
     */
    boolean isFile();

    /**
     * 是否链接
     *
     * @return 结果
     */
    boolean isLink();

    /**
     * 获取拥有者
     *
     * @return 拥有者
     */
    String getOwner();

    /**
     * 获取分组
     *
     * @return 分组
     */
    String getGroup();

    /**
     * 获取文件大小
     *
     * @return 文件大小
     */
    long getFileSize();

    /**
     * 设置文件大小
     *
     * @param fileSize 文件大小
     */
    void setFileSize(long fileSize);

    /**
     * 获取文件大小，显示
     *
     * @return 文件大小
     */
    default String getFileSizeDisplay() {
        if (this.isDirectory() || this.isReturnDirectory() || this.isCurrentFile()) {
            return "-";
        }
        return NumberUtil.formatSize(this.getFileSize(), 2);
    }

    /**
     * 获取文件名称
     *
     * @return 文件名称
     */
    String getFileName();

    /**
     * 设置文件名称
     *
     * @param fileName 文件名称
     */
    void setFileName(String fileName);

    /**
     * 是否目录
     *
     * @return 结果
     */
    boolean isDirectory();

    /**
     * 获取父路径
     *
     * @return 父路径
     */
    String getParentPath();

    /**
     * 获取权限
     *
     * @return 权限
     */
    String getPermissions();

    /**
     * 设置权限
     *
     * @param permissions 权限
     */
    void setPermissions(String permissions);

    /**
     * 获取修改时间
     *
     * @return 修改时间
     */
    String getModifyTime();

    /**
     * 设置修改时间
     *
     * @param modifyTime 修改时间
     */
    void setModifyTime(String modifyTime);

    /**
     * 获取文件排序
     *
     * @return 文件排序
     */
    default int getFileOrder() {
        if (this.isReturnDirectory()) {
            return -10;
        }
        if (this.isLink()) {
            return -9;
        }
        if (this.isDirectory() && this.isHiddenFile()) {
            return -8;
        }
        if (this.isFile() && this.isHiddenFile()) {
            return -7;
        }
        if (this.isDirectory()) {
            return -6;
        }
        return 0;
    }

    /**
     * 获取文件路径
     *
     * @return 文件路径
     */
    default String getFilePath() {
        return ShellFileUtil.concat(this.getParentPath(), this.getFileName());
    }

    /**
     * 获取图标
     *
     * @return 图标
     */
    default SVGGlyph getIcon() {
        SVGGlyph glyph;
        if (this.isReturnDirectory()) {
            glyph = new ReturnFolderSVGGlyph();
        } else if (this.isLink() && this.isDirectory()) {
            glyph = new FolderLinkSVGGlyph();
        } else if (this.isLink() && this.isFile()) {
            glyph = new FileLinkSVGGlyph();
        } else if (this.isLink()) {
            glyph = new FileLinkSVGGlyph();
        } else if (this.isDirectory()) {
            glyph = new FolderSVGGlyph();
        } else {
            glyph = new FileSVGGlyph();
        }
        if (this.isHiddenFile()) {
            glyph.setOpacity(0.5);
        }
        return glyph;
    }

//    /**
//     * 刷新图标
//     */
//    void refreshIcon();

    /**
     * 开始等待动画
     */
    default void startWaiting() {
        SVGGlyph icon = this.getIcon();
        if (icon != null) {
            icon.startWaiting();
        }
    }

    /**
     * 结束等待动画
     */
    default void stopWaiting() {
        SVGGlyph icon = this.getIcon();
        if (icon != null) {
            icon.stopWaiting();
        }
    }

    /**
     * 等待动画是否开启中
     *
     * @return 结果
     */
    default boolean isWaiting() {
        SVGGlyph icon = this.getIcon();
        if (icon != null) {
            return icon.isWaiting();
        }
        return false;
    }

    /**
     * 是否当前"文件"
     *
     * @return 结果
     */
    default boolean isCurrentFile() {
        return ".".equals(this.getFileName());
    }

    /**
     * 是否返回"文件"
     *
     * @return 结果
     */
    default boolean isReturnDirectory() {
        return "..".equals(this.getFileName());
    }

    /**
     * 是否正常文件
     *
     * @return 结果
     */
    default boolean isNormal() {
        return !this.isCurrentFile() && !this.isReturnDirectory();
    }

    /**
     * 是否隐藏文件
     *
     * @return 结果
     */
    default boolean isHiddenFile() {
        if (this.isReturnDirectory() || this.isCurrentFile()) {
            return false;
        }
        return this.getFileName().startsWith(".");
    }

    /**
     * 是否有当前用户读取权限
     *
     * @return 结果
     */
    default boolean hasOwnerReadPermission() {
        return ShellFileUtil.hasOwnerReadPermission(this.getPermissions());
    }

    /**
     * 是否有当前用户写入权限
     *
     * @return 结果
     */
    default boolean hasOwnerWritePermission() {
        return ShellFileUtil.hasOwnerWritePermission(this.getPermissions());
    }

    /**
     * 是否有当前用户执行权限
     *
     * @return 结果
     */
    default boolean hasOwnerExecutePermission() {
        return ShellFileUtil.hasOwnerExecutePermission(this.getPermissions());
    }

    /**
     * 是否有组用户读取权限
     *
     * @return 结果
     */
    default boolean hasGroupsReadPermission() {
        return ShellFileUtil.hasGroupsReadPermission(this.getPermissions());
    }

    /**
     * 是否有组用户写入权限
     *
     * @return 结果
     */
    default boolean hasGroupsWritePermission() {
        return ShellFileUtil.hasGroupsWritePermission(this.getPermissions());
    }

    /**
     * 是否有组用户执行权限
     *
     * @return 结果
     */
    default boolean hasGroupsExecutePermission() {
        return ShellFileUtil.hasGroupsExecutePermission(this.getPermissions());
    }

    /**
     * 是否有其他用户读取权限
     *
     * @return 结果
     */
    default boolean hasOthersReadPermission() {
        return ShellFileUtil.hasOthersReadPermission(this.getPermissions());
    }

    /**
     * 是否有其他用户写入权限
     *
     * @return 结果
     */
    default boolean hasOthersWritePermission() {
        return ShellFileUtil.hasOthersWritePermission(this.getPermissions());
    }

    /**
     * 是否有其他用户执行权限
     *
     * @return 结果
     */
    default boolean hasOthersExecutePermission() {
        return ShellFileUtil.hasOthersExecutePermission(this.getPermissions());
    }

    /**
     * 是否根目录
     *
     * @return 结果
     */
    default boolean isRoot() {
        return this.isDirectory() && "/".equals(this.getFilePath());
    }

    /**
     * 获取扩展名
     *
     * @return 扩展名
     */
    default String getExtName() {
        return FileNameUtil.extName(this.getFileName());
    }
}
