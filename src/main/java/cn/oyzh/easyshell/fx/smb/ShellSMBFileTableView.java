package cn.oyzh.easyshell.fx.smb;

import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.fx.file.ShellFileTableView;
import cn.oyzh.easyshell.smb.ShellSMBClient;
import cn.oyzh.easyshell.smb.ShellSMBFile;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.collections.ListChangeListener;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-07-23
 */
public class ShellSMBFileTableView extends ShellFileTableView<ShellSMBClient, ShellSMBFile> implements FXEventListener {

    @Override
    public void setClient(ShellSMBClient client) {
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
        List<MenuItem> menuItems = new ArrayList<>(super.getMenuItems());
        menuItems.add(MenuItemHelper.separator());
        // 初始化上传菜单
        menuItems.add(super.initUploadMenu());
        // 获取选中的文件
        List<ShellSMBFile> files = this.getFilterSelectedItems();
        // 下载文件
        if (!files.isEmpty()) {
            FXMenuItem downloadFile = MenuItemHelper.downloadFile("12", () -> this.downloadFile(files));
            menuItems.add(downloadFile);
        }
        return menuItems;
    }

    @Override
    public boolean isSupportPermissionAction() {
        return false;
    }
}
