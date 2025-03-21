package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.sftp.transport.SftpTransportTask;
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
public class SftpTransportTaskTableView extends FXTableView<SftpTransportTask> {

    @Override
    protected void initTableView() {
        super.initTableView();
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
        List<SftpTransportTask> tasks = this.getSelectedItems();
        if (CollectionUtil.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem cancelTransport = MenuItemHelper.cancelTransport("12", ()->{
            for (SftpTransportTask task : tasks) {
                task.cancel();
            }
            this.removeItem(tasks);
        });
        MenuItem removeTransport = MenuItemHelper.removeTransport("12", ()->{
            for (SftpTransportTask task : tasks) {
                task.remove();
            }
            this.removeItem(tasks);
        });
        menuItems.add(cancelTransport);
        menuItems.add(removeTransport);
        return menuItems;
    }
}
