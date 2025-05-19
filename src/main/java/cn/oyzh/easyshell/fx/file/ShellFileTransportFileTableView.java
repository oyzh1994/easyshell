package cn.oyzh.easyshell.fx.file;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.collections.ListChangeListener;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellFileTransportFileTableView extends ShellFileTableView<ShellFileClient<ShellFile>, ShellFile> {

    private Consumer<List<ShellFile>> transportCallback;

    public Consumer<List<ShellFile>> getTransportCallback() {
        return transportCallback;
    }

    public void setTransportCallback(Consumer<List<ShellFile>> transportCallback) {
        this.transportCallback = transportCallback;
    }

    @Override
    public void setClient(ShellFileClient client) {
        super.setClient(client);
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
        //     return super.getMenuItems();
        // }
        // 获取选中的文件
        List<ShellFile> files = this.getFilterSelectedItems();
        List<MenuItem> menuItems = new ArrayList<>();
        // 传输文件
        FXMenuItem transportFile = MenuItemHelper.transportFile("12", () -> this.transportFile(files));
        transportFile.setDisable(files.isEmpty());
        menuItems.add(transportFile);
        menuItems.add(MenuItemHelper.separator());
        // 添加父级菜单
        menuItems.addAll(super.getMenuItems());
        return menuItems;
    }

    /**
     * 传输文件
     *
     * @param files 文件列表
     */
    private void transportFile(List<ShellFile> files) {
        if (this.transportCallback == null) {
            return;
        }
        files = files.stream().filter(file -> !this.checkInvalid(file)).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        this.transportCallback.accept(files);
    }

//    @Override
//    public void filePermission(ShellFile file) {
//        Object client = this.client;
//        // ftp
//        if (file instanceof ShellFTPFile) {
//            ShellViewFactory.ftpFilePermission(file, (ShellFTPClient) client);
//        } else if (file instanceof ShellSFTPFile) {// sftp
//            ShellViewFactory.sftpFilePermission(file, (ShellSFTPClient) client);
//        }
//    }

//    @Override
//    public void editFile(ShellFile file) {
//        if (!ShellFileUtil.fileEditable(file)) {
//            return;
//        }
//            ShellViewFactory.fileEdit(file,  this.client);
//        this.onFileSaved(file);
//    }
}
