package cn.oyzh.easyshell.fx.file;

import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.menu.FXMenuItem;
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

    }

    @Override
    public void cd(String filePath) {

    }

    @Override
    public void editFile(ShellFile file) {

    }

    @Override
    public void touch(String name) throws Exception {

    }

    @Override
    public void mkdir(String name) throws Exception {

    }
}
