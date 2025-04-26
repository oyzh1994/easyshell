package cn.oyzh.easyshell.fx.ftp;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.ftp.ShellFTPDownloadFile;
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
public class ShellFTPDownloadFileTableView extends FXTableView<ShellFTPDownloadFile> {

    @Override
    public void initNode() {
        super.initNode();
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
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
        List<ShellFTPDownloadFile> tasks = this.getSelectedItems();
        if (CollectionUtil.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem cancelTransport = MenuItemHelper.cancelDownload("12", () -> {
            for (ShellFTPDownloadFile task : tasks) {
                task.cancel();
            }
            this.removeItem(tasks);
        });
        menuItems.add(cancelTransport);
        return menuItems;
    }

}
