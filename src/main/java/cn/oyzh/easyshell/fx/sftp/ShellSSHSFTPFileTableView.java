package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.sftp2.ShellSFTPFile;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.ShellUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.CompressSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DeleteSVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-05-26
 */
public class ShellSSHSFTPFileTableView extends ShellSFTPFileTableView {

    /**
     * 是否打包传输
     */
    private boolean pkgTransfer;

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

    public boolean isPkgTransfer() {
        return pkgTransfer;
    }

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
            //    boolean isAllDir = true;
            //    // 判断是否都是文件夹
            //    for (ShellSFTPFile file : files) {
            //        if (!file.isDirectory()) {
            //            isAllDir = false;
            //            break;
            //        }
            //    }
            // if (isAllDir) {
            //    FXMenuItem forceDel = (FXMenuItem) MenuItemManager.getMenuItem(this.client.isWindows() ? "rmdir /s /q" : "rm -rf", new DeleteSVGGlyph("12"), () -> this.forceDel(files));
            //    menuItems.add(forceDel);
            //}
            FXMenuItem forceDel = (FXMenuItem) MenuItemManager.getMenuItem(this.client.isWindows() ? "rmdir /s /q" : "rm -rf", new DeleteSVGGlyph("12"), () -> this.forceDel(files));
            menuItems.add(forceDel);
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
                MenuItem menuItem8 = MenuItemHelper.menuItem("zip", () -> this.compress(files, "zip"));
                MenuItem menuItem9 = MenuItemHelper.menuItem("rar", () -> this.compress(files, "rar"));
                MenuItem menuItem10 = MenuItemHelper.menuItem("7z", () -> this.compress(files, "7z"));
                menu.getItems().add(menuItem1);
                menu.getItems().add(menuItem2);
                menu.getItems().add(menuItem3);
                menu.getItems().add(menuItem4);
                menu.getItems().add(menuItem5);
                menu.getItems().add(menuItem6);
                menu.getItems().add(menuItem7);
                menu.getItems().add(menuItem8);
                menu.getItems().add(menuItem9);
                menu.getItems().add(menuItem10);
                menuItems.add(menu);
            }
        }
        if (!menuItems.isEmpty()) {
            menuItems.add(MenuItemHelper.separator());
        }
        // 添加父级菜单
        menuItems.addAll(super.getMenuItems());

        // 打包传输
        // MenuItem packageTransfer = MenuItemHelper.menuItem(I18nHelper.packageTransfer(), this.pkgTransfer ? new SubmitSVGGlyph("12") : null, this::packageTransfer);
        CheckMenuItem packageTransfer = new CheckMenuItem(I18nHelper.packageTransfer());
        packageTransfer.setSelected(this.pkgTransfer);
        packageTransfer.setOnAction(event -> this.packageTransfer());
        menuItems.add(packageTransfer);
        return menuItems;
    }

    /**
     * 打包传输
     */
    private void packageTransfer() {
        this.pkgTransfer = !this.pkgTransfer;
    }

    /**
     * 强制删除
     *
     * @param files 文件列表
     */
    protected void forceDel(List<ShellSFTPFile> files) {
        // 提示
        if (!MessageBox.confirm(ShellI18nHelper.fileTip20())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                // linux、macos、unix下合并路径操作
                if (this.sshClient.isLinux() || this.sshClient.isMacos() || this.sshClient.isUnix()) {
                    List<String> list = files.parallelStream().map(ShellSFTPFile::getFilePath).toList();
                    // 执行操作
                    String result = this.sshClient.serverExec().forceDel(list);
                    if (StringUtil.isNotBlank(result)) {
                        MessageBox.warn(result);
                    } else {
                        for (ShellSFTPFile file : files) {
                            super.onFileDeleted(file.getFilePath());
                        }
                    }
                } else if (this.sshClient.isWindows()) {// 正常操作
                    for (ShellSFTPFile file : files) {
                        // 执行操作
                        String result = this.sshClient.serverExec().forceDel(file.getFilePath(), file.isFile());
                        if (StringUtil.isNotBlank(result)) {
                            MessageBox.warn(result);
                        } else {
                            super.onFileDeleted(file.getFilePath());
                        }
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
                    String result = this.sshClient.serverExec().compress(file.getFilePath(), type);
                    if (ShellUtil.isCommandNotFound(result)) {
                        MessageBox.warn(result);
                        break;
                    }
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
                    String result = this.sshClient.serverExec().uncompress(file.getFilePath());
                    if (ShellUtil.isCommandNotFound(result)) {
                        MessageBox.warn(result);
                        break;
                    }
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

    /**
     * 打包上传
     *
     * @param files     文件列表
     * @param sshClient ssh客户端
     */
    public void updateByPkg(List<File> files, ShellSSHClient sshClient) {
        String dest = this.getLocation();
        StageAdapter adapter = ShellViewFactory.filePkgUpload(this.getLocation(), files, this.client);
        if (adapter != null && adapter.hasProp("compressFile")) {
            File compressFile = adapter.getProp("compressFile");
            String remoteFile = ShellFileUtil.concat(dest, compressFile.getName());
            this.uploadFile(compressFile, aBoolean -> {
                if (aBoolean) {
                    try {
                        sshClient.serverExec().uncompress(remoteFile);
                        this.client.delete(remoteFile);
                        this.reloadFile();
                    } catch (Exception ex) {
                        MessageBox.exception(ex);
                    }
                } else {
                    MessageBox.warn(I18nHelper.uploadFailed());
                }
            });
        }
    }

}
