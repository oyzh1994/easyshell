package cn.oyzh.easyshell.fx.file;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.file.ShellFileTransportTask;
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
 * @since 2025-03-21
 */
public class ShellFileTransportTaskTableView extends FXTableView<ShellFileTransportTask> {

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
        List<ShellFileTransportTask> tasks = this.getSelectedItems();
        if (CollectionUtil.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem cancel = MenuItemHelper.cancelTransport("12", () -> {
            for (ShellFileTransportTask sftpTransportTask : new ArrayList<>(tasks)) {
                sftpTransportTask.cancel();
            }
            // this.removeItem(tasks);
        });
        menuItems.add(cancel);
        ShellFileTransportTask task = tasks.getFirst();
        MenuItem retry = MenuItemHelper.retry("12", task::retry);
        retry.setDisable(tasks.size() != 1 || !task.isFailed());
        menuItems.add(retry);
        return menuItems;
    }

    /**
     * 取消
     */
    public void cancel() {
        List<ShellFileTransportTask> tasks = new ArrayList<>(this.getSelectedItems());
        for (ShellFileTransportTask task : tasks) {
            task.cancel();
        }
    }
}
