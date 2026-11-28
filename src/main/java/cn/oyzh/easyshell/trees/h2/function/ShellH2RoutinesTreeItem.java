package cn.oyzh.easyshell.trees.h2.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.routine.H2Routine;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeItem;
import cn.oyzh.easyshell.trees.h2.schema.ShellH2SchemaTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * H2 函数/过程分组节点。
 */
public class ShellH2RoutinesTreeItem extends ShellH2TreeItem<ShellH2RoutinesTreeItemValue> {

    private final H2RoutineKind kind;

    protected ShellH2RoutinesTreeItem(RichTreeView treeView, H2RoutineKind kind) {
        super(treeView);
        super.setFilterable(true);
        this.kind = kind;
        this.setValue(new ShellH2RoutinesTreeItemValue(this));
    }

    @Override
    public ShellH2SchemaTreeItem parent() {
        return (ShellH2SchemaTreeItem) super.parent();
    }

    public H2RoutineKind kind() {
        return this.kind;
    }

    public String schema() {
        return this.parent().schema();
    }

    public ShellH2Client client() {
        return this.parent().client();
    }

    public ShellConnect info() {
        return this.parent().info();
    }

    public Integer getRoutineSize() {
        return !this.isLoaded() || this.isLoading() ? null : this.unfilteredChildrenSize();
    }

    @Override
    public void loadChild() {
        if (this.isLoading() || this.isLoaded()) {
            return;
        }
        this.setLoading(true);
        try {
            this.clearChild();
            List<H2Routine> routines = this.kind == H2RoutineKind.FUNCTION
                    ? this.client().selectFunctions(this.schema())
                    : this.client().selectProcedures(this.schema());
            List<TreeItem<?>> items = new ArrayList<>();
            for (H2Routine routine : routines) {
                items.add(new ShellH2RoutineTreeItem(routine, this.getTreeView()));
            }
            this.setChild(items);
            this.setLoaded(true);
            this.doFilter();
            this.doSort();
            this.expend();
            this.refresh();
        } finally {
            this.setLoading(false);
        }
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        if (this.kind == H2RoutineKind.FUNCTION) {
            items.add(MenuItemHelper.addFunction(this::addFunction));
        } else {
            items.add(MenuItemHelper.addProcedure(this::addProcedure));
        }
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(reload);
        return items;
    }

    private void addFunction() {
        H2Routine function = new H2Routine();
        function.setSchema(this.schema());
        function.setType("FUNCTION");
        this.parent().parent().tabPane().openRoutineDesign(function, this);
    }

    private void addProcedure() {
        H2Routine procedure = new H2Routine();
        procedure.setSchema(this.schema());
        procedure.setType("PROCEDURE");
        this.parent().parent().tabPane().openRoutineDesign(procedure, this);
    }

    /**
     * 添加函数节点。
     *
     * @param function 函数
     */
    public void addFunction(H2Routine function) {
        this.addRoutine(function, H2RoutineKind.FUNCTION);
    }

    public void addProcedure(H2Routine procedure) {
        this.addRoutine(procedure, H2RoutineKind.PROCEDURE);
    }

    private void addRoutine(H2Routine routine, H2RoutineKind expectedKind) {
        if (routine == null || this.kind != expectedKind
                || StringUtil.isBlank(routine.getName())) {
            return;
        }
        boolean exists = this.richChildren().stream()
                .filter(ShellH2RoutineTreeItem.class::isInstance)
                .map(ShellH2RoutineTreeItem.class::cast)
                .anyMatch(item -> StringUtil.equalsIgnoreCase(
                        item.routineName(), routine.getName()));
        if (!exists) {
            this.addChild(new ShellH2RoutineTreeItem(routine, this.getTreeView()));
            this.doFilter();
            this.doSort();
            this.refresh();
        }
    }
}
