package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.sftp2.ShellSFTPFile;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.CompressSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DeleteSVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-05-26
 */
public class ShellSSHSFTPFileTableView extends ShellSFTPFileTableView {

    /**
     * 临时文件类型
     * 1 剪切
     * 2 复制
     */
    private byte tempFileType;

    /**
     * 临时文件，可能是剪切或者复制的文件
     */
    private List<ShellSFTPFile> tempFiles;

    /**
     * ssh客户端
     */
    private ShellSSHClient sshClient;

    public void setSSHClient(ShellSSHClient sshClient) {
        this.sshClient = sshClient;
        this.setClient(sshClient.sftpClient());
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        // 获取选中的文件
        List<ShellSFTPFile> files = this.getFilterSelectedItems();
        List<MenuItem> menuItems = new ArrayList<>();
        // 复制文件
        if (!files.isEmpty()) {
            FXMenuItem copyFile = MenuItemHelper.copyFile("12", () -> this.copyFile(files));
            menuItems.add(copyFile);
            // 剪切文件
            FXMenuItem cutFile = MenuItemHelper.cutFile("12", () -> this.cutFile(files));
            menuItems.add(cutFile);
        }
        // 粘贴文件
        if (CollectionUtil.isNotEmpty(this.tempFiles)) {
            ShellFile f = this.tempFiles.getFirst();
            if (StringUtil.notEquals(f.getParentPath(), this.getLocation())) {
                FXMenuItem pasteFile = MenuItemHelper.pasteFile("12", this::pasteFile);
                menuItems.add(pasteFile);
            }
        }
        // 强制删除
        if (CollectionUtil.isNotEmpty(files)) {
            boolean isAllDir = true;
            // 判断是否都是文件夹
            for (ShellSFTPFile file : files) {
                if (!file.isDirectory()) {
                    isAllDir = false;
                    break;
                }
            }
            if (isAllDir) {
                FXMenuItem forceDel = (FXMenuItem) MenuItemManager.getMenuItem(this.client.isWindows() ? "rmdir /s /q" : "rm -rf", new DeleteSVGGlyph("12"), () -> this.forceDel(files));
                menuItems.add(forceDel);
            }
        }
        // 解压文件
        if (this.client.isLinux() && CollectionUtil.isNotEmpty(files)) {
            boolean isAllNormal = true;
            boolean isAllCompress = true;
            // 判断是否都是压缩包、文件夹
            for (ShellSFTPFile file : files) {
                String extName = FileNameUtil.extName(file.getFileName());
                boolean isCompress = FileNameUtil.isGzType(extName)
                        || FileNameUtil.isXzType(extName)
                        || FileNameUtil.isLzType(extName)
                        || FileNameUtil.isZstType(extName)
                        || FileNameUtil.isLzoType(extName)
                        || FileNameUtil.isLzmaType(extName)
                        || FileNameUtil.isZipType(extName)
                        || FileNameUtil.is7zType(extName)
                        || FileNameUtil.isRarType(extName);
                if (!isCompress) {
                    isAllCompress = false;
                }
                if (!file.isNormal()) {
                    isAllNormal = false;
                }
                if (!isAllCompress && !isAllNormal) {
                    break;
                }
            }
            // 解压
            if (isAllCompress) {
                FXMenuItem unCompress = MenuItemHelper.unCompress("12", () -> this.uncompress(files));
                menuItems.add(unCompress);
            } else if (isAllNormal) {// 压缩文件或者文件夹
                Menu menu = MenuItemHelper.menu(I18nHelper.compress(), new CompressSVGGlyph("12"));
                MenuItem menuItem1 = MenuItemHelper.menuItem("tar.gz", () -> this.compress(files, "tar.gz"));
                MenuItem menuItem2 = MenuItemHelper.menuItem("tar", () -> this.compress(files, "tar"));
                MenuItem menuItem3 = MenuItemHelper.menuItem("bz2", () -> this.compress(files, "bz2"));
                MenuItem menuItem4 = MenuItemHelper.menuItem("xz", () -> this.compress(files, "xz"));
                MenuItem menuItem5 = MenuItemHelper.menuItem("lz", () -> this.compress(files, "lz"));
                MenuItem menuItem6 = MenuItemHelper.menuItem("lzo", () -> this.compress(files, "lzo"));
                MenuItem menuItem7 = MenuItemHelper.menuItem("zst", () -> this.compress(files, "zst"));
                menu.getItems().add(menuItem1);
                menu.getItems().add(menuItem2);
                menu.getItems().add(menuItem3);
                menu.getItems().add(menuItem4);
                menu.getItems().add(menuItem5);
                menu.getItems().add(menuItem6);
                menu.getItems().add(menuItem7);
                menuItems.add(menu);
            }
        }
        if (!menuItems.isEmpty()) {
            menuItems.add(MenuItemHelper.separator());
        }
        // 添加父级菜单
        menuItems.addAll(super.getMenuItems());
        return menuItems;
    }

    /**
     * 强制删除
     *
     * @param files 文件列表
     */
    protected void forceDel(List<ShellSFTPFile> files) {
        StageManager.showMask(() -> {
            try {
                for (ShellSFTPFile file : files) {
                    if (!file.isDirectory()) {
                        continue;
                    }
                    // 提示
                    if (!MessageBox.confirm(I18nHelper.deleteFile() + " " + file.getFileName() + "?")) {
                        continue;
                    }
                    // 执行操作
                    String result = this.sshClient.serverExec().forceDel(file.getFilePath());
                    if (StringUtil.isNotBlank(result)) {
                        MessageBox.warn(result);
                    } else {
                        super.onFileDeleted(file.getFilePath());
                    }
                }
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 压缩文件
     *
     * @param type  压缩类型
     * @param files 文件列表
     */
    protected void compress(List<ShellSFTPFile> files, String type) {
        StageManager.showMask(() -> {
            try {
                // 执行解压
                for (ShellSFTPFile file : files) {
                    this.sshClient.serverExec().compress(file.getFilePath(), type);
                }
                super.loadFileInnerBatch();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 解压文件
     *
     * @param files 文件列表
     */
    protected void uncompress(List<ShellSFTPFile> files) {
        StageManager.showMask(() -> {
            try {
                // 执行解压
                for (ShellSFTPFile file : files) {
                    this.sshClient.serverExec().uncompress(file.getFilePath());
                }
                super.loadFileInnerBatch();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 剪切文件
     *
     * @param files 文件列表
     */
    protected void cutFile(List<ShellSFTPFile> files) {
        this.tempFiles = files;
        this.tempFileType = 1;
    }

    /**
     * 复制文件
     *
     * @param files 文件列表
     */
    protected void copyFile(List<ShellSFTPFile> files) {
        this.tempFiles = files;
        this.tempFileType = 2;
    }

    /**
     * 粘贴文件
     */
    protected void pasteFile() {
        StageManager.showMask(() -> {
            try {
                for (ShellSFTPFile f1 : this.tempFiles) {
                    String fName = ShellFileUtil.concat(this.getLocation(), f1.getFileName());
                    String dst = f1.isDirectory() ? this.getLocation() : fName;
                    // 判断文件是否存在
                    if (this.client.exist(fName) && !MessageBox.confirm("[" + fName + "] " + ShellI18nHelper.fileTip4())) {
                        continue;
                    }
                    String result;
                    // 剪切
                    if (this.tempFileType == 1) {
                        result = this.sshClient.serverExec().move(f1.getFilePath(), dst);
                    } else {// 复制
                        result = this.sshClient.serverExec().copy(f1.getFilePath(), dst);
                    }
                    if (StringUtil.isNotBlank(result)) {
                        MessageBox.warn(result);
                    } else {
                        super.onFileAdded(fName);
                    }
                }
            } catch (Exception ex) {
                MessageBox.exception(ex);
            } finally {
                this.tempFiles = null;
            }
        });
    }

}
