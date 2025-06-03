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

//    @Override
//    public void initNode() {
//        super.initNode();
//        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//    }
//
//    @Override
//    protected void initEvenListener() {
//        super.initEvenListener();
//        // 右键菜单事件
//        this.setOnContextMenuRequested(e -> {
//            List<? extends MenuItem> items = this.getMenuItems();
//            if (CollectionUtil.isNotEmpty(items)) {
//                this.showContextMenu(items, e.getScreenX() - 10, e.getScreenY() - 10);
//            } else {
//                this.clearContextMenu();
//            }
//        });
//        this.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);
//        // 初始化鼠标多选辅助类
//        TableViewMouseSelectHelper.install(this);
//        // 快捷键
//        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//            // 删除
//            if (KeyboardUtil.delete_keyCombination.match(event)) {
//                this.deleteFile(this.getSelectedItems());
//                event.consume();
//            } else if (KeyboardUtil.rename_keyCombination.match(event)) {// 重命名
//                this.renameFile(this.getSelectedItems());
//                event.consume();
//            } else if (KeyboardUtil.refresh_keyCombination.match(event)) {// 刷新
//                this.loadFile();
//                event.consume();
//            }
//        });
//    }
//
//    private boolean showHiddenFile = false;
//
//    public void setShowHiddenFile(boolean showHiddenFile) {
//        this.showHiddenFile = showHiddenFile;
//        this.refreshFile();
//    }
//
//    private String filterText;
//
//    public void setFilterText(String filterText) {
//        if (!StringUtil.equals(this.filterText, filterText)) {
//            this.filterText = filterText;
//            this.refreshFile();
//        }
//    }

//    protected ShellFTPClient client;
//
//    public ShellFTPClient getClient() {
//        return client;
//    }

    @Override
    public void setClient(ShellFTPClient client) {
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
        // 上传文件
        FXMenuItem uploadFile = MenuItemHelper.uploadFile("12", this::uploadFile);
        // 上传文件夹
        FXMenuItem uploadFolder = MenuItemHelper.uploadFolder("12", this::uploadFolder);
        menuItems.add(uploadFile);
        menuItems.add(uploadFolder);
        // 获取选中的文件
        List<ShellFTPFile> files = this.getFilterSelectedItems();
        // 下载文件
        if (!files.isEmpty()) {
            FXMenuItem downloadFile = MenuItemHelper.downloadFile("12", () -> this.downloadFile(files));
            menuItems.add(downloadFile);
        }
        return menuItems;
    }
}
