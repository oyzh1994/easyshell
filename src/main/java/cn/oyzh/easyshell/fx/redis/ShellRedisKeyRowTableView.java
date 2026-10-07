package cn.oyzh.easyshell.fx.redis;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.redis.key.ShellRedisKeyRow;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * redis键行表格
 *
 * @author oyzh
 * @since 2025/05/20
 */
public class ShellRedisKeyRowTableView<R extends ShellRedisKeyRow> extends FXTableView<R> {

    /**
     * 新增动作
     */
    private Runnable addAction;

    /**
     * 获取新增动作
     *
     * @return 新增动作
     */
    public Runnable getAddAction() {
        return addAction;
    }

    /**
     * 设置新增动作
     *
     * @param addAction 新增动作
     */
    public void setAddAction(Runnable addAction) {
        this.addAction = addAction;
    }

    /**
     * 复制动作
     */
    private Runnable copyAction;

    /**
     * 获取复制动作
     *
     * @return 复制动作
     */
    public Runnable getCopyAction() {
        return copyAction;
    }

    /**
     * 设置复制动作
     *
     * @param copyAction 复制动作
     */
    public void setCopyAction(Runnable copyAction) {
        this.copyAction = copyAction;
    }

    /**
     * 删除动作
     */
    private Runnable deleteAction;

    /**
     * 获取删除动作
     *
     * @return 删除动作
     */
    public Runnable getDeleteAction() {
        return deleteAction;
    }

    /**
     * 设置删除动作
     *
     * @param deleteAction 删除动作
     */
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
                this.showContextMenu(items, e.getScreenX(), e.getScreenY());
            } else {
                this.clearContextMenu();
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<FXMenuItem> menuItems = new ArrayList<>();
        List<R> rows = this.getSelectedItems();
        FXMenuItem add = MenuItemHelper.add(this.addAction);
        menuItems.add(add);

        FXMenuItem copy = MenuItemHelper.copy(this.copyAction);
        copy.setDisable(rows.isEmpty());
        menuItems.add(copy);

        FXMenuItem delete = MenuItemHelper.delete(this.deleteAction);
        delete.setDisable(rows.isEmpty());
        menuItems.add(delete);

        return menuItems;
    }
}
