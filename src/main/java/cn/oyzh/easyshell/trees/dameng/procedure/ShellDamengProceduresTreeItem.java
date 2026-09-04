package cn.oyzh.easyshell.trees.dameng.procedure;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.ShellDamengTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * db树过程类型节点
 *
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellDamengProceduresTreeItem extends ShellDamengTreeItem<ShellDamengProceduresTreeItemValue> {

    public ShellDamengProceduresTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellDamengProceduresTreeItemValue(this));
    }

    @Override
    public ShellDamengSchemaTreeItem parent() {
        return (ShellDamengSchemaTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addProcedure(this::add);
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(add);
        items.add(reload);
        return items;
    }

    private void add() {
        DamengProcedure procedure = new DamengProcedure();
        procedure.setSchema(this.schema());
        ShellDamengEventUtil.designProcedure(procedure, this.parent());
    }

    @Override
    public boolean itemVisible() {
        return this.isVisible();
    }

    /**
     * 加载子节点
     */
    public void loadChild() {
        if (!this.isWaiting() && !this.isLoaded() && !this.isLoading()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<DamengProcedure> procedures = this.client().selectProceduresSimple(this.schema());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (DamengProcedure procedure : procedures) {
                                list.add(new ShellDamengProcedureTreeItem(procedure, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellDamengProcedureTreeItem> list = children;
                            List<ShellDamengProcedureTreeItem> delList = new ArrayList<>();
                            List<ShellDamengProcedureTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellDamengProcedureTreeItem item : list) {
                                if (procedures.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (DamengProcedure f : procedures) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellDamengProcedureTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellDamengProcedureTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    procedures.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
                                }
                            }
                            list.removeAll(delList);
                            list.addAll(addList);
                        }
                    })
                    .onSuccess(this::expend)
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex);
                    })
                    .onFinish(() -> {
                        this.setLoading(false);
                        this.doFilter();
                        this.doSort();
                    })
                    .build();
            // 执行业务
            this.startWaiting(task);
        }
    }

    @Override
    public void reloadChild() {
        this.clearProcedureSize();
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    public String schema() {
        return this.parent().schema();
    }

    public ShellDamengClient client() {
        return this.parent().client();
    }

    public ShellConnect info() {
        return this.parent().info();
    }

    public String infoName() {
        return this.parent().infoName();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    public int procedureSize() {
        try {
            return this.client().procedureSize(this.schema());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private Integer procedureSize;

    public Integer getProcedureSize() {
        if (this.procedureSize == null) {
            this.procedureSize = this.procedureSize();
        }
        return this.procedureSize;
    }

    public void addProcedure(DamengProcedure procedure) {
        this.addChild(new ShellDamengProcedureTreeItem(procedure, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearProcedureSize();
    }

    public void clearProcedureSize() {
        this.procedureSize = null;
    }
}
