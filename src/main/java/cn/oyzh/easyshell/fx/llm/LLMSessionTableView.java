package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.llm.LLMSession;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM会话表格
 *
 * @author oyzh
 * @since 2026-10-31
 */
public class LLMSessionTableView extends FXTableView<LLMSession> {

    private Runnable restoreAction;

    private Runnable editAction;

    private Runnable deleteAction;

    public Runnable getRestoreAction() {
        return restoreAction;
    }

    public void setRestoreAction(Runnable restoreAction) {
        this.restoreAction = restoreAction;
    }

    public Runnable getEditAction() {
        return editAction;
    }

    public void setEditAction(Runnable editAction) {
        this.editAction = editAction;
    }

    public Runnable getDeleteAction() {
        return deleteAction;
    }

    public void setDeleteAction(Runnable deleteAction) {
        this.deleteAction = deleteAction;
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        this.setOnContextMenuRequested(event -> {
            List<? extends MenuItem> items = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(items)) {
                this.showContextMenu(items, event.getScreenX(), event.getScreenY());
            } else {
                this.clearContextMenu();
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<FXMenuItem> menuItems = new ArrayList<>();
        List<LLMSession> rows = this.getSelectedItems();
        FXMenuItem restore = MenuItemHelper.restore(this.restoreAction);
        restore.setDisable(rows.isEmpty());
        menuItems.add(restore);

        FXMenuItem edit = MenuItemHelper.edit(this.editAction);
        edit.setDisable(rows.isEmpty());
        menuItems.add(edit);

        FXMenuItem delete = MenuItemHelper.delete(this.deleteAction);
        delete.setDisable(rows.isEmpty());
        menuItems.add(delete);
        return menuItems;
    }
}
