package cn.oyzh.easyshell.fx.s3;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.fx.file.ShellFileTableView;
import cn.oyzh.easyshell.s3.ShellS3Client;
import cn.oyzh.easyshell.s3.ShellS3File;
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
public class ShellS3FileTableView extends ShellFileTableView<ShellS3Client, ShellS3File> implements FXEventListener {

    @Override
    public void setClient(ShellS3Client client) {
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
        List<MenuItem> menuItems = new ArrayList<>();
        if (this.isSupportUploadAction()) {
            // 上传文件
            FXMenuItem uploadFile = MenuItemHelper.uploadFile("12", this::uploadFile);
            // 上传文件夹
            FXMenuItem uploadFolder = MenuItemHelper.uploadFolder("12", this::uploadFolder);
            menuItems.add(uploadFolder);
            menuItems.add(uploadFile);
        }
        // 获取选中的文件
        List<ShellS3File> files = this.getFilterSelectedItems();
        // 下载文件
        if (!files.isEmpty()) {
            if (this.isSupportDownloadAction()) {
                FXMenuItem downloadFile = MenuItemHelper.downloadFile("12", () -> this.downloadFile(files));
                menuItems.add(downloadFile);
            }
        }
        if (menuItems.isEmpty()) {
            return super.getMenuItems();
        }
        List<MenuItem> list = new ArrayList<>(super.getMenuItems());
        list.add(MenuItemHelper.separator());
        list.addAll(menuItems);
        return list;
    }

    /**
     * 是否根路径
     *
     * @return 结果
     */
    protected boolean isRootLocation() {
        String location = this.getLocation();
        if (location == null || location.isEmpty()) {
            return true;
        }
        return StringUtil.equals(location, "/");
    }

    @Override
    public boolean isSupportAction(String action) {
        if (this.isRootLocation()) {
            return false;
        }
        if ("mkdir".equals(action)) {
            return false;
        }
        if ("permission".equals(action)) {
            return false;
        }
        return super.isSupportAction(action);
    }
}
