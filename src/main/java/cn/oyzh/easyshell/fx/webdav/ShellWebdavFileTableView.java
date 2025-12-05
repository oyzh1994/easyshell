package cn.oyzh.easyshell.fx.webdav;

import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.fx.file.ShellFileTableView;
import cn.oyzh.easyshell.webdav.ShellWebdavClient;
import cn.oyzh.easyshell.webdav.ShellWebdavFile;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.collections.ListChangeListener;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-10-09
 */
public class ShellWebdavFileTableView extends ShellFileTableView<ShellWebdavClient, ShellWebdavFile> implements FXEventListener {

    private ListChangeListener<ShellFileUploadTask> uploadTaskListener = (ListChangeListener<ShellFileUploadTask>) change -> {
        change.next();
        if (change.wasRemoved()) {
            for (ShellFileUploadTask task : change.getRemoved()) {
                if (!task.isFailed() && !task.isCanceled()) {
                    this.onFileAdded(task.getDestPath());
                }
            }
        }
    };

    private ListChangeListener<ShellFileDeleteTask> deleteTaskListener = (ListChangeListener<ShellFileDeleteTask>) change -> {
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
    public void setClient(ShellWebdavClient client) {
        super.setClient(client);
        this.client.uploadTasks().addListener(this.uploadTaskListener);
        this.client.deleteTasks().addListener(this.deleteTaskListener);
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>(super.getMenuItems());
        menuItems.add(MenuItemHelper.separator());
        // 初始化上传菜单
        menuItems.add(super.initUploadMenu());
        // 获取选中的文件
        List<ShellWebdavFile> files = this.getFilterSelectedItems();
        // 下载文件
        if (!files.isEmpty()) {
            FXMenuItem downloadFile = MenuItemHelper.downloadFile("12", () -> this.downloadFile(files));
            menuItems.add(downloadFile);
        }
        return menuItems;
    }

    @Override
    public void destroy() {
        this.client.uploadTasks().removeListener(this.uploadTaskListener);
        this.client.deleteTasks().removeListener(this.deleteTaskListener);
        this.uploadTaskListener = null;
        this.deleteTaskListener = null;
        super.destroy();
    }
}
