package cn.oyzh.easyshell.fx.file;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.ftp.ShellFTPClient;
import cn.oyzh.easyshell.ftp.ShellFTPFile;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.util.ShellFileUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.collections.ListChangeListener;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
                    this.onFileDeleted(task.getFilePath());
                }
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<ShellFile> files = this.getSelectedItems();
        // 检查是否包含无效文件
        if (this.checkInvalid(files)) {
            return super.getMenuItems();
        }
        List<MenuItem> menuItems = new ArrayList<>();
        // 传输文件
        FXMenuItem transportFile = MenuItemHelper.transportFile("12", () -> this.transportFile(files));
        menuItems.add(transportFile);
        menuItems.add(MenuItemHelper.separator());
        // 添加父级菜单
        menuItems.addAll(super.getMenuItems());
        // 禁用未实现的功能
        for (MenuItem menuItem : menuItems) {
            if (StringUtil.equalsAny(menuItem.getId(), "touchFile", "createDir")) {
                menuItem.setDisable(true);
            }
        }
        return menuItems;
    }

    /**
     * 传输文件
     *
     * @param files 文件列表
     */
    private void transportFile(List<ShellFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        if (this.transportCallback == null) {
            return;
        }
        this.transportCallback.accept(files);
    }

    @Override
    public void filePermission(ShellFile file) {
        Object client = this.client;
        // ftp
        if (file instanceof ShellFTPFile) {
            ShellViewFactory.ftpFilePermission(file, (ShellFTPClient) client);
        } else if (file instanceof ShellSFTPFile) {// sftp
            ShellViewFactory.sftpFilePermission(file, (ShellSFTPClient) client);
        }
    }

    @Override
    public void editFile(ShellFile file) {
        if (!ShellFileUtil.fileEditable(file)) {
            return;
        }
        Object client = this.client;
        // ftp
        if (file instanceof ShellFTPFile) {
            ShellViewFactory.ftpFileEdit(file, (ShellFTPClient) client);
        } else if (file instanceof ShellSFTPFile) {// sftp
            ShellViewFactory.sftpFileEdit(file, (ShellSFTPClient) client);
        }
        this.onFileSaved(file);
    }

    @Override
    public void touch(String name) throws Exception {

    }

    @Override
    public void createDir(String name) throws Exception {

    }
}
