package cn.oyzh.easyshell.fx.file;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
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
public class ShellFileUploadTaskTableView extends FXTableView<ShellFileUploadTask> {

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
        List<ShellFileUploadTask> tasks = this.getSelectedItems();
        if (CollectionUtil.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        // List<ShellFileUploadTask> list = new ArrayList<>(tasks);
        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem cancel = MenuItemHelper.cancelUpload("12", () -> {
            for (ShellFileUploadTask task : tasks) {
                task.cancel();
            }
            // this.removeItem(list);
        });
        menuItems.add(cancel);
        ShellFileUploadTask task = tasks.getFirst();
        MenuItem retry = MenuItemHelper.retry("12", task::retry);
        retry.setDisable(tasks.size() != 1 || !task.isFailed());
        menuItems.add(retry);
        return menuItems;
    }
}
