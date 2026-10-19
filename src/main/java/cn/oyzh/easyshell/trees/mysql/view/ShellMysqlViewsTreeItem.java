package cn.oyzh.easyshell.trees.mysql.view;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mysql.ShellMysqlEventUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.view.MysqlView;
import cn.oyzh.easyshell.trees.mysql.ShellMysqlTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
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
 * mysql视图类型节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlViewsTreeItem extends ShellMysqlTreeItem<ShellMysqlViewsTreeItemValue> {

    /**
     * 构造视图类型节点
     *
     * @param treeView 树视图
     */
    public ShellMysqlViewsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMysqlViewsTreeItemValue(this));
    }

    @Override
    public ShellMysqlDatabaseTreeItem parent() {
        return (ShellMysqlDatabaseTreeItem) super.parent();
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
        MysqlView dbView = new MysqlView();
        dbView.setDbName(this.dbName());
        ShellMysqlEventUtil.designView(dbView, this.parent());
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
                        List<MysqlView> views = this.client().selectViewsSimple(this.dbName());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MysqlView view : views) {
                                list.add(new ShellMysqlViewTreeItem(view, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellMysqlViewTreeItem> list = children;
                            List<ShellMysqlViewTreeItem> delList = new ArrayList<>();
                            List<ShellMysqlViewTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellMysqlViewTreeItem item : list) {
                                if (views.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (MysqlView f : views) {
                                if (list.parallelStream().noneMatch(item -> f.compare(item.value()))) {
                                    addList.add(new ShellMysqlViewTreeItem(f, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellMysqlViewTreeItem item : list) {
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
        this.clearViewSize();
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
    public ShellMysqlClient client() {
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
     * 视图数量缓存
     */
    private Integer viewSize;

    /**
     * 获取视图数量（带缓存）
     *
     * @return 视图数量
     */
    public Integer getViewSize() {
        if (this.viewSize == null) {
            this.viewSize = this.viewSize();
        }
        return this.viewSize;
    }

    /**
     * 获取mysql信息
     *
     * @return mysql信息
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
     * 新增视图并刷新排序
     *
     * @param view 视图对象
     */
    public void addView(MysqlView view) {
        this.addChild(new ShellMysqlViewTreeItem(view, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearViewSize();
    }

    /**
     * 清空视图数量缓存
     */
    public void clearViewSize() {
        this.viewSize = null;
    }
}
