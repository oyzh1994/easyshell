package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.fx.file.ShellFileTableView;
import cn.oyzh.easyshell.ssh.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.ssh.sftp.ShellSFTPFile;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.collections.ListChangeListener;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellSFTPFileTableView extends ShellFileTableView<ShellSFTPClient, ShellSFTPFile> implements FXEventListener {

    @Override
    public void setClient(ShellSFTPClient client) {
        super.setClient(client);
        this.client.uploadTasks().addListener((ListChangeListener<ShellFileUploadTask>) change -> {
            change.next();
            if (change.wasRemoved()) {
                for (ShellFileUploadTask task : change.getRemoved()) {
                    if (!task.isFailed() && !task.isCanceled()) {
                        this.onFileAdded(task.getDestPath());
                    }
                }
            }
        });
        this.client.deleteTasks().addListener((ListChangeListener<ShellFileDeleteTask>) change -> {
            change.next();
            if (change.wasRemoved()) {
                for (ShellFileDeleteTask task : change.getRemoved()) {
                    if (!task.isFailed() && !task.isCanceled()) {
                        this.onFileDeleted(task.getFilePath());
                    }
                }
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        // if (CollectionUtil.isEmpty(files)) {
        //     return Collections.emptyList();
        // }
        // // 检查是否包含无效文件
        // if (this.checkInvalid(files)) {
        //     return Collections.emptyList();
        // }
        List<MenuItem> menuItems = new ArrayList<>(super.getMenuItems());
//        // 创建文件
//        FXMenuItem touch = MenuItemHelper.touchFile("12", this::touch);
//        menuItems.add(touch);
//        // 创建文件夹
//        FXMenuItem mkdir = FXMenuItem.newItem(I18nHelper.mkdir(), new FolderSVGGlyph("12"), this::mkdir);
//        menuItems.add(mkdir);
//        menuItems.add(MenuItemHelper.separator());
//        if (files.size() == 1) {
//            ShellSFTPFile file = files.getFirst();
//            // 编辑文件
//            FXMenuItem editFile = MenuItemHelper.editFile("12", () -> this.editFile(file));
//            if (!ShellFileUtil.fileEditable(file)) {
//                editFile.setDisable(true);
//            }
//            editFile.setAccelerator(KeyboardUtil.edit_keyCombination);
//            menuItems.add(editFile);
//            // 文件信息
//            FXMenuItem fileInfo = MenuItemHelper.fileInfo("12", () -> this.fileInfo(file));
//            fileInfo.setAccelerator(KeyboardUtil.info_keyCombination);
//            menuItems.add(fileInfo);
//            // 复制路径
//            FXMenuItem copyFilePath = MenuItemHelper.copyFilePath("12", () -> this.copyFilePath(file));
//            menuItems.add(copyFilePath);
//            // 重命名文件
//            FXMenuItem renameFile = MenuItemHelper.renameFile("12", () -> this.renameFile(files));
//            renameFile.setAccelerator(KeyboardUtil.rename_keyCombination);
//            menuItems.add(renameFile);
//            // 文件权限
//            FXMenuItem filePermission = MenuItemHelper.filePermission("12", () -> this.filePermission(file));
//            menuItems.add(filePermission);
//            menuItems.add(MenuItemHelper.separator());
//        }
//        // 刷新文件
//        FXMenuItem refreshFile = MenuItemHelper.refreshFile("12", this::loadFile);
//        refreshFile.setAccelerator(KeyboardUtil.refresh_keyCombination);
//        menuItems.add(refreshFile);
//        // 删除文件
//        FXMenuItem deleteFile = MenuItemHelper.deleteFile("12", () -> this.deleteFile(files));
//        deleteFile.setAccelerator(KeyboardUtil.delete_keyCombination);
//        menuItems.add(deleteFile);
        menuItems.add(MenuItemHelper.separator());
        // 上传文件
        FXMenuItem uploadFile = MenuItemHelper.uploadFile("12", this::uploadFile);
        // 上传文件夹
        FXMenuItem uploadFolder = MenuItemHelper.uploadFolder("12", this::uploadFolder);
        menuItems.add(uploadFile);
        menuItems.add(uploadFolder);
        // 获取选中的文件
        List<ShellSFTPFile> files = this.getFilterSelectedItems();
        // 下载文件
        if (!files.isEmpty()) {
            FXMenuItem downloadFile = MenuItemHelper.downloadFile("12", () -> this.downloadFile(files));
            menuItems.add(downloadFile);
        }
        return menuItems;
    }
}
