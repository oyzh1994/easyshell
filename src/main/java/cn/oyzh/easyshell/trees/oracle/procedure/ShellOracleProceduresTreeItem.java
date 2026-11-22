package cn.oyzh.easyshell.trees.oracle.procedure;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.procedure.OracleProcedure;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.oracle.ShellOracleEventUtil;
import cn.oyzh.easyshell.trees.oracle.ShellOracleTreeItem;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
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
 * Oracle数据库树过程类型节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleProceduresTreeItem extends ShellOracleTreeItem<ShellOracleProceduresTreeItemValue> {

    /**
     * 构造Oracle数据库树过程类型节点
     *
     * @param treeView 树视图
     */
    public ShellOracleProceduresTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellOracleProceduresTreeItemValue(this));
    }

    @Override
    public ShellOracleSchemaTreeItem parent() {
        return (ShellOracleSchemaTreeItem) super.parent();
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
        OracleProcedure procedure = new OracleProcedure();
        procedure.setSchema(this.schema());
        ShellOracleEventUtil.designProcedure(procedure, this.parent());
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
                        List<OracleProcedure> procedures = this.client().selectProceduresSimple(this.schema());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (OracleProcedure procedure : procedures) {
                                list.add(new ShellOracleProcedureTreeItem(procedure, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellOracleProcedureTreeItem> list = children;
                            List<ShellOracleProcedureTreeItem> delList = new ArrayList<>();
                            List<ShellOracleProcedureTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellOracleProcedureTreeItem item : list) {
                                if (procedures.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (OracleProcedure f : procedures) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellOracleProcedureTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellOracleProcedureTreeItem item : list) {
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
     * 获取Oracle数据库客户端
     *
     * @return Oracle数据库客户端
     */
    public ShellOracleClient client() {
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

    public Integer getCachedProcedureSize() {
        return this.procedureSize;
    }

    public void setProcedureSize(Integer procedureSize) {
        this.procedureSize = procedureSize;
    }

    /**
     * 添加过程
     *
     * @param procedure 过程
     */
    public void addProcedure(OracleProcedure procedure) {
        this.addChild(new ShellOracleProcedureTreeItem(procedure, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.procedureSize = this.incrementCount(this.procedureSize);
    }

    /**
     * 清空过程数量缓存
     */
    public void clearProcedureSize() {
        this.procedureSize = null;
    }
}
