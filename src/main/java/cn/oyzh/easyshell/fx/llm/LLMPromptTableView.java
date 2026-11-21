package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.llm.prompt.LLMPrompt;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户提示词表格。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public class LLMPromptTableView extends FXTableView<LLMPrompt> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }

    private Runnable addAction;

    private Runnable editAction;

    private Runnable deleteAction;

    public void setAddAction(Runnable addAction) {
        this.addAction = addAction;
    }

    public void setEditAction(Runnable editAction) {
        this.editAction = editAction;
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
        boolean empty = this.getSelectedItems().isEmpty();
        menuItems.add(MenuItemHelper.add(this.addAction));
        FXMenuItem edit = MenuItemHelper.edit(this.editAction);
        edit.setDisable(empty);
        menuItems.add(edit);
        FXMenuItem delete = MenuItemHelper.delete(this.deleteAction);
        delete.setDisable(empty);
        menuItems.add(delete);
        return menuItems;
    }
}
