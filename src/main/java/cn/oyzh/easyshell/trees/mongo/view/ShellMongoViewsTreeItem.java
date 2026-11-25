package cn.oyzh.easyshell.trees.mongo.view;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.view.MongoView;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
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
 * MongoDB 视图类型节点。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoViewsTreeItem extends ShellMongoTreeItem<ShellMongoViewsTreeItemValue> {

    public ShellMongoViewsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMongoViewsTreeItemValue(this));
    }

    @Override
    public ShellMongoDatabaseTreeItem parent() {
        return (ShellMongoDatabaseTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem add = MenuItemHelper.addView(this::addView);
        FXMenuItem reload = MenuItemHelper.reloadData(this::reloadChild);
        items.add(add);
        items.add(reload);
        return items;
    }

    /**
     * 新增视图
     */
    private void addView() {
        MongoView view = new MongoView();
        view.setDbName(this.dbName());
        ShellMongoEventUtil.designView(view, this.parent());
    }

    @Override
    public boolean itemVisible() {
        return this.isVisible();
    }

    @Override
    public void loadChild() {
        if (!this.isLoading() && !this.isLoaded()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<MongoView> views = this.client().listViews(this.dbName());
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MongoView view : views) {
                                list.add(new ShellMongoViewTreeItem(view, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {
                            ObservableList<ShellMongoViewTreeItem> list = (ObservableList) this.richChildren();
                            List<ShellMongoViewTreeItem> delList = new ArrayList<>();
                            List<ShellMongoViewTreeItem> addList = new ArrayList<>();
                            for (ShellMongoViewTreeItem item : list) {
                                if (views.parallelStream().noneMatch(view -> view.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            for (MongoView view : views) {
                                if (list.parallelStream().noneMatch(item -> view.compare(item.value()))) {
                                    addList.add(new ShellMongoViewTreeItem(view, this.getTreeView()));
                                }
                            }
                            for (ShellMongoViewTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    views.parallelStream()
                                            .filter(view -> view.compare(item.value()))
                                            .findFirst()
                                            .ifPresent(view -> item.value().copy(view));
                                }
                            }
                            list.removeAll(delList);
                            list.addAll(addList);
                        }
                        this.expend();
                    })
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex, this.getWindow());
                    })
                    .onSuccess(this::refresh)
                    .onFinish(() -> {
                        this.setLoading(false);
                        this.doFilter();
                        this.doSort();
                    })
                    .build();
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

    public String dbName() {
        return this.parent().dbName();
    }

    public ShellMongoClient client() {
        return this.parent().client();
    }

    public ShellConnect info() {
        return this.parent().info();
    }

    public String connectName() {
        return this.parent().connectName();
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
     * 新增视图并刷新排序。
     *
     * @param view 视图
     */
    public void addView(MongoView view) {
        this.addChild(new ShellMongoViewTreeItem(view, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearViewSize();
    }

    /**
     * 获取视图数量。
     *
     * @return 视图数量
     */
    public long viewSize() {
        try {
            return this.client().listViews(this.dbName()).size();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private Integer viewSize;

    public synchronized Integer getViewSize() {
        if (this.viewSize == null) {
            this.viewSize = Math.toIntExact(this.viewSize());
        }
        return this.viewSize;
    }

    public void clearViewSize() {
        this.viewSize = null;
    }
}
