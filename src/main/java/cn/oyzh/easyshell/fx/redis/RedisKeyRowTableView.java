package cn.oyzh.easyshell.fx.redis;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.redis.key.RedisKeyRow;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025/05/20
 */
public class RedisKeyRowTableView<R extends RedisKeyRow> extends FXTableView<R> {

    private Runnable addAction;

    public Runnable getAddAction() {
        return addAction;
    }

    public void setAddAction(Runnable addAction) {
        this.addAction = addAction;
    }

    private Runnable copyAction;

    public Runnable getCopyAction() {
        return copyAction;
    }

    public void setCopyAction(Runnable copyAction) {
        this.copyAction = copyAction;
    }

    private Runnable deleteAction;

    public Runnable getDeleteAction() {
        return deleteAction;
    }

    public void setDeleteAction(Runnable deleteAction) {
        this.deleteAction = deleteAction;
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<? extends MenuItem> items = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(items)) {
                this.showContextMenu(items, e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<FXMenuItem> menuItems = new ArrayList<>();
        List<R> rows = this.getSelectedItems();
        FXMenuItem add = MenuItemHelper.add("12", this.addAction);
        menuItems.add(add);

        FXMenuItem copy = MenuItemHelper.copy("12", this.copyAction);
        copy.setDisable(rows.isEmpty());
        menuItems.add(copy);

        FXMenuItem delete = MenuItemHelper.delete("12", this.deleteAction);
        delete.setDisable(rows.isEmpty());
        menuItems.add(delete);

        return menuItems;
    }
}
