package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.llm.skill.LLMSkill;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent Skill 表格。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public class LLMSkillTableView extends FXTableView<LLMSkill> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }

    private Runnable addAction;

    private Runnable editAction;

    private Runnable deleteAction;

    private Runnable exportAction;

    public void setAddAction(Runnable addAction) {
        this.addAction = addAction;
    }

    public void setEditAction(Runnable editAction) {
        this.editAction = editAction;
    }

    public void setDeleteAction(Runnable deleteAction) {
        this.deleteAction = deleteAction;
    }

    public void setExportAction(Runnable exportAction) {
        this.exportAction = exportAction;
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
        List<MenuItem> items = new ArrayList<>();
        boolean empty = this.getSelectedItems().isEmpty();
        items.add(MenuItemHelper.add(this.addAction));
        MenuItem edit = MenuItemHelper.edit(this.editAction);
        edit.setDisable(empty);
        items.add(edit);
        MenuItem delete = MenuItemHelper.delete(this.deleteAction);
        delete.setDisable(empty);
        items.add(delete);
        MenuItem export = MenuItemHelper.menuItem(
                I18nResourceBundle.i18nString("shell.llm.skill.export"), this.exportAction);
        export.setDisable(empty);
        items.add(export);
        return items;
    }
}
