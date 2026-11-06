package cn.oyzh.easyshell.trees.postgresql.procedure;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlProcedure;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.postgresql.ShellPostgresqlEventUtil;
import cn.oyzh.easyshell.trees.postgresql.ShellPostgresqlTreeItem;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
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
 * PostgreSQL数据库树过程类型节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlProceduresTreeItem extends ShellPostgresqlTreeItem<ShellPostgresqlProceduresTreeItemValue> {

    /**
     * 构造PostgreSQL数据库树过程类型节点
     *
     * @param treeView 树视图
     */
    public ShellPostgresqlProceduresTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellPostgresqlProceduresTreeItemValue(this));
    }

    @Override
    public ShellPostgresqlSchemaTreeItem parent() {
        return (ShellPostgresqlSchemaTreeItem) super.parent();
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

    /**
     * 新增过程
     */
    private void add() {
        PostgresqlProcedure procedure = new PostgresqlProcedure();
        procedure.setSchema(this.schema());
        ShellPostgresqlEventUtil.designProcedure(procedure, this.parent());
    }

    @Override
    public boolean itemVisible() {
        return this.isVisible();
    }

    @Override
    public void loadChild() {
        if (!this.isWaiting() && !this.isLoaded() && !this.isLoading()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<PostgresqlProcedure> procedures = this.client().selectProceduresSimple(this.schema());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (PostgresqlProcedure procedure : procedures) {
                                list.add(new ShellPostgresqlProcedureTreeItem(procedure, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellPostgresqlProcedureTreeItem> list = children;
                            List<ShellPostgresqlProcedureTreeItem> delList = new ArrayList<>();
                            List<ShellPostgresqlProcedureTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellPostgresqlProcedureTreeItem item : list) {
                                if (procedures.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (PostgresqlProcedure f : procedures) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellPostgresqlProcedureTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellPostgresqlProcedureTreeItem item : list) {
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
                        MessageBox.exception(ex, this.getWindow());
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

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.parent().schema();
    }

    /**
     * 获取PostgreSQL数据库客户端
     *
     * @return PostgreSQL数据库客户端
     */
    public ShellPostgresqlClient client() {
        return this.parent().client();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
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

    /**
     * 获取过程数量
     *
     * @return 过程数量
     */
    public int procedureSize() {
        try {
            return this.client().procedureSize(this.schema());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 过程数量
     */
    private Integer procedureSize;

    /**
     * 获取过程数量
     *
     * @return 过程数量
     */
    public Integer getProcedureSize() {
        if (this.procedureSize == null) {
            this.procedureSize = this.procedureSize();
        }
        return this.procedureSize;
    }

    /**
     * 添加过程
     *
     * @param procedure 过程
     */
    public void addProcedure(PostgresqlProcedure procedure) {
        this.addChild(new ShellPostgresqlProcedureTreeItem(procedure, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearProcedureSize();
    }

    /**
     * 清空过程数量缓存
     */
    public void clearProcedureSize() {
        this.procedureSize = null;
    }
}
