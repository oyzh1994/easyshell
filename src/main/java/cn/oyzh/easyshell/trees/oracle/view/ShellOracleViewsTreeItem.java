package cn.oyzh.easyshell.trees.oracle.view;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.view.OracleView;
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
 * Oracle数据库树视图类型节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleViewsTreeItem extends ShellOracleTreeItem<ShellOracleViewsTreeItemValue> {

    /**
     * 构造Oracle数据库树视图类型节点
     *
     * @param treeView 树视图
     */
    public ShellOracleViewsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellOracleViewsTreeItemValue(this));
    }

    @Override
    public ShellOracleSchemaTreeItem parent() {
        return (ShellOracleSchemaTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addView(this::add);
        items.add(add);
        FXMenuItem reload = MenuItemHelper.refreshData(this::reloadChild);
        items.add(reload);
        return items;
    }

    /**
     * 新增视图
     */
    private void add() {
        OracleView dbView = new OracleView();
        dbView.setSchema(this.schema());
        ShellOracleEventUtil.designView(dbView, this.parent());
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
                        List<OracleView> views = this.client().selectViewsSimple(this.schema());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (OracleView view : views) {
                                list.add(new ShellOracleViewTreeItem(view, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellOracleViewTreeItem> list = children;
                            List<ShellOracleViewTreeItem> delList = new ArrayList<>();
                            List<ShellOracleViewTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellOracleViewTreeItem item : list) {
                                if (views.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (OracleView f : views) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellOracleViewTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellOracleViewTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    views.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
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
        this.clearViewSize();
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
     * 获取视图数量
     *
     * @return 视图数量
     */
    public int viewSize() {
        try {
            return this.parent().viewSize();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 视图数量
     */
    private Integer viewSize;

    /**
     * 获取视图数量
     *
     * @return 视图数量
     */
    public Integer getViewSize() {
        if (this.viewSize == null) {
            this.viewSize = this.viewSize();
        }
        return this.viewSize;
    }

    public Integer getCachedViewSize() {
        return this.viewSize;
    }

    public void setViewSize(Integer viewSize) {
        this.viewSize = viewSize;
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
     * 添加视图
     *
     * @param view 视图
     */
    public void addView(OracleView view) {
        if (view == null) {
            return;
        }
        this.addChild(new ShellOracleViewTreeItem(view, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.viewSize = this.incrementCount(this.viewSize);
    }

    /**
     * 清空视图数量缓存
     */
    public void clearViewSize() {
        this.viewSize = null;
    }
}
