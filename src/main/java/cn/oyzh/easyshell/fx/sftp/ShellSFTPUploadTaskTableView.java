package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.sftp.ShellSFTPUploadTask;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewMouseSelectHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellSFTPUploadTaskTableView extends FXTableView<ShellSFTPUploadTask> {

    @Override
    public void initNode() {
        super.initNode();
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<? extends MenuItem> menuItems = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(menuItems)) {
                this.showContextMenu(menuItems, e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
        // 初始化鼠标多选辅助类
        TableViewMouseSelectHelper.install(this);
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<ShellSFTPUploadTask> tasks = this.getSelectedItems();
        if (CollectionUtil.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        List<ShellSFTPUploadTask> list = new ArrayList<>(tasks);
        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem cancelTransport = MenuItemHelper.cancelUpload("12", () -> {
            for (ShellSFTPUploadTask task : list) {
                task.cancel();
            }
            this.removeItem(list);
        });
        menuItems.add(cancelTransport);
        return menuItems;
    }
}
