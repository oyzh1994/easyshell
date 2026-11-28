package cn.oyzh.easyshell.trees.h2.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.routine.H2Routine;
import cn.oyzh.easyshell.trees.h2.schema.ShellH2SchemaTreeItem;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeItem;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * H2 函数/过程节点。
 */
public class ShellH2RoutineTreeItem extends ShellH2TreeItem<ShellH2RoutineTreeItemValue> {

    private final H2Routine value;

    public ShellH2RoutineTreeItem(H2Routine value, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = value;
        this.setValue(new ShellH2RoutineTreeItemValue(this));
    }

    public H2Routine value() {
        return this.value;
    }

    public String routineName() {
        return this.value.getName();
    }

    @Override
    public ShellH2RoutinesTreeItem parent() {
        return (ShellH2RoutinesTreeItem) super.parent();
    }

    public ShellH2SchemaTreeItem schemaItem() {
        return this.parent().parent();
    }

    public String schema() {
        return this.parent().schema();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        if (this.parent().kind() == H2RoutineKind.FUNCTION) {
            items.add(MenuItemHelper.designFunction(this::design));
            items.add(MenuItemHelper.renameFunction(this::rename));
            items.add(MenuItemHelper.deleteFunction(this::delete));
            items.add(MenuItemHelper.separator());
            items.add(MenuItemHelper.cloneFunction(this::cloneFunction));
        } else if (this.parent().kind() == H2RoutineKind.PROCEDURE) {
            items.add(MenuItemHelper.designProcedure(this::design));
            items.add(MenuItemHelper.renameProcedure(this::rename));
            items.add(MenuItemHelper.deleteProcedure(this::delete));
            items.add(MenuItemHelper.separator());
            items.add(MenuItemHelper.cloneProcedure(this::cloneFunction));
        }
        return items;
    }

    private void design() {
        this.schemaItem().parent().tabPane().openRoutineDesign(
                this.value, this.parent());
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (this.parent().kind() == H2RoutineKind.FUNCTION
                || this.parent().kind() == H2RoutineKind.PROCEDURE) {
            this.design();
        }
    }

    @Override
    public void rename() {
        String newName = MessageBox.prompt(I18nHelper.pleaseInputName(),
                this.routineName(), this.getWindow());
        if (newName == null || Objects.equals(newName, this.routineName())) {
            return;
        }
        if (StringUtil.isBlank(newName)) {
            MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
            return;
        }
        try {
            String oldName = this.routineName();
            H2Routine function = this.schemaItem().client().renameFunction(
                    this.schema(), oldName, newName);
            this.schemaItem().parent().tabPane().closeFunctionDesignTabs(
                    this.schema(), oldName);
            this.value().setName(function.getName());
            this.parent().reloadChild();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.confirmDeleteFunction(this.routineName()),
                this.getWindow())) {
            return;
        }
        try {
            this.schemaItem().client().dropFunction(this.schema(), this.routineName());
            this.schemaItem().parent().tabPane().closeFunctionDesignTabs(
                    this.schema(), this.routineName());
            this.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    private void cloneFunction() {
        StageManager.showMask(this.getWindow(), () -> {
            try {
                String cloneName = this.routineName() + DBUtil.genCloneName();
                this.schemaItem().client().cloneFunction(
                        this.schema(), this.routineName(), cloneName);
                this.parent().reloadChild();
            } catch (Exception ex) {
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }
}
