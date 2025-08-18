package cn.oyzh.easyshell.fx.file;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.util.ShellViewFactory;
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
        List<ShellFileUploadTask> list = new ArrayList<>(tasks);
        List<MenuItem> menuItems = new ArrayList<>();
        // 取消
        MenuItem cancel = MenuItemHelper.cancelUpload("12", () -> {
            for (ShellFileUploadTask task : list) {
                task.cancel();
            }
        });
        menuItems.add(cancel);

        // 重试
        MenuItem retry = MenuItemHelper.retry("12", () -> this.retry(list));
        menuItems.add(retry);

        // 错误
        ShellFileUploadTask task = list.getFirst();
        MenuItem errorInfo = MenuItemHelper.errorInfo("12", () -> this.errorInfo(task));
        errorInfo.setDisable(list.size() != 1 || !task.isFailed());
        menuItems.add(errorInfo);
        return menuItems;
    }

    /**
     * 重试
     *
     * @param tasks 任务列表
     */
    protected void retry(List<ShellFileUploadTask> tasks) {
        for (ShellFileUploadTask task : tasks) {
            if (task.isFailed()) {
                task.retry();
            }
        }
    }

    /**
     * 错误信息
     *
     * @param task 任务
     */
    protected void errorInfo(ShellFileUploadTask task) {
        ShellViewFactory.fileError(task);
    }
}
