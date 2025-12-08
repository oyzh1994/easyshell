package cn.oyzh.easyshell.fx.ftp;

import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.ftp.ShellFTPClient;
import cn.oyzh.easyshell.ftp.ShellFTPFile;
import cn.oyzh.easyshell.fx.file.ShellFileTableView;
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
public class ShellFTPFileTableView extends ShellFileTableView<ShellFTPClient, ShellFTPFile> implements FXEventListener {

    private ListChangeListener<ShellFileUploadTask> uploadTaskListener = change -> {
        change.next();
        if (change.wasRemoved()) {
            for (ShellFileUploadTask task : change.getRemoved()) {
                if (!task.isFailed() && !task.isCanceled()) {
                    this.onFileAdded(task.getDestPath());
                }
            }
        }
    };

    private ListChangeListener<ShellFileDeleteTask> deleteTaskListener = change -> {
        change.next();
        if (change.wasRemoved()) {
            for (ShellFileDeleteTask task : change.getRemoved()) {
                if (!task.isFailed() && !task.isCanceled()) {
                    this.onFileDeleted(task.getFilePath());
                }
            }
        }
    };

    @Override
    public void setClient(ShellFTPClient client) {
        super.setClient(client);
        this.client.uploadTasks().addListener(this.uploadTaskListener);
        this.client.deleteTasks().addListener(this.deleteTaskListener);
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>(super.getMenuItems());
        menuItems.add(MenuItemHelper.separator());
        // // 上传文件
        // FXMenuItem uploadFile = MenuItemHelper.uploadFile("12", this::uploadFile);
        // // 上传文件夹
        // FXMenuItem uploadFolder = MenuItemHelper.uploadFolder("12", this::uploadFolder);
        // menuItems.add(uploadFile);
        // menuItems.add(uploadFolder);
        // 初始化上传菜单
        menuItems.add(super.initUploadMenu());
        // 获取选中的文件
        List<ShellFTPFile> files = this.getFilterSelectedItems();
        // 下载文件
        if (!files.isEmpty()) {
            FXMenuItem downloadFile = MenuItemHelper.downloadFile("12", () -> this.downloadFile(files));
            menuItems.add(downloadFile);
        }
        return menuItems;
    }

    @Override
    public void destroy() {
        if (this.client != null) {
            this.client.uploadTasks().removeListener(this.uploadTaskListener);
            this.client.deleteTasks().removeListener(this.deleteTaskListener);
        }
        this.uploadTaskListener = null;
        this.deleteTaskListener = null;
        super.destroy();
    }
}
