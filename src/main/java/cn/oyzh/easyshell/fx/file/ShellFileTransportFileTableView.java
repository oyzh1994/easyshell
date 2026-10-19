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
 * 文件传输文件表格视图
 *
 * @author oyzh
 * @since 2025-03-21
 */
public class ShellFileTransportFileTableView extends ShellFileTableView<ShellFileClient<ShellFile>, ShellFile> {

    /**
     * 传输回调
     */
    private Consumer<List<ShellFile>> transportCallback;

    /**
     * 获取传输回调
     *
     * @return 传输回调
     */
    public Consumer<List<ShellFile>> getTransportCallback() {
        return transportCallback;
    }

    /**
     * 设置传输回调
     *
     * @param transportCallback 传输回调
     */
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
        // 获取选中的文件
        List<ShellFile> files = this.getFilterSelectedItems();
        List<MenuItem> menuItems = new ArrayList<>();
        if (!files.isEmpty()) {
            // 传输文件
            FXMenuItem transportFile = MenuItemHelper.transportFile( () -> this.transportFile(files));
            menuItems.add(transportFile);
            menuItems.add(MenuItemHelper.separator());
        }
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
}
