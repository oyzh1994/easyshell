package cn.oyzh.easyshell.trees.mariadb.procedure;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.procedure.MariadbProcedure;
import cn.oyzh.easyshell.trees.mariadb.ShellMariadbTreeItem;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
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
 * MariaDB过程类型节点
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbProceduresTreeItem extends ShellMariadbTreeItem<ShellMariadbProceduresTreeItemValue> {

    /**
     * 构造过程类型节点
     *
     * @param treeView 树视图
     */
    public ShellMariadbProceduresTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMariadbProceduresTreeItemValue(this));
    }

    @Override
    public ShellMariadbDatabaseTreeItem parent() {
        return (ShellMariadbDatabaseTreeItem) super.parent();
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
        MariadbProcedure procedure = new MariadbProcedure();
        procedure.setDbName(this.dbName());
        ShellMariadbEventUtil.designProcedure(procedure, this.parent());
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
                        List<MariadbProcedure> procedures = this.client().selectProceduresSimple(this.dbName());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MariadbProcedure procedure : procedures) {
                                list.add(new ShellMariadbProcedureTreeItem(procedure, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellMariadbProcedureTreeItem> list = children;
                            List<ShellMariadbProcedureTreeItem> delList = new ArrayList<>();
                            List<ShellMariadbProcedureTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellMariadbProcedureTreeItem item : list) {
                                if (procedures.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (MariadbProcedure f : procedures) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellMariadbProcedureTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellMariadbProcedureTreeItem item : list) {
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

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.parent().dbName();
    }

    /**
     * 获取db客户端
     *
     * @return db客户端
     */
    public ShellMariadbClient client() {
        return this.parent().client();
    }

    /**
     * 获取MariaDB信息
     *
     * @return MariaDB信息
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
            return this.client().procedureSize(this.dbName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 过程数量缓存
     */
    private Integer procedureSize;

    /**
     * 获取过程数量（带缓存）
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
     * 新增过程并刷新排序
     *
     * @param procedure 过程对象
     */
    public void addProcedure(MariadbProcedure procedure) {
        this.addChild(new ShellMariadbProcedureTreeItem(procedure, this.getTreeView()));
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
