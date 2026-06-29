package cn.oyzh.easyshell.trees.mongo.collection;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.MongoCollection;
import cn.oyzh.easyshell.trees.mongo.MongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.easyshell.util.mongo.MongoViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * db树表类型节点
 *
 * @author oyzh
 * @since 2023/12/08
 */
public class MongoCollectionsTreeItem extends MongoTreeItem<MongoCollectionsTreeItemValue> {

    public MongoCollectionsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new MongoCollectionsTreeItemValue(this));
        super.unfilteredChildren().addListener((ListChangeListener<TreeItem<?>>) change -> {
            this.collectionsSize = null;
        });
    }

    @Override
    public MongoDatabaseTreeItem parent() {
        return (MongoDatabaseTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem reload = MenuItemHelper.reloadData( this::reloadChild);
        FXMenuItem add = MenuItemHelper.addCollection( this::addCollection);
        FXMenuItem exportData = MenuItemHelper.exportData( this::exportData);
        FXMenuItem importData = MenuItemHelper.importData( this::importData);
        items.add(add);
        items.add(reload);
        items.add(exportData);
        items.add(importData);
        return items;
    }

    /**
     * 导出数据
     */
    private void exportData() {
        MongoViewFactory.exportData(this.client(), this.dbName(), null);
    }

    /**
     * 导入数据
     */
    private void importData() {
        MongoViewFactory.importData(this.client(), this.dbName());
    }

    private void addCollection() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputCollectionName());
        if (StringUtil.isBlank(name)) {
            return;
        }
        MongoCollection collection = new MongoCollection();
        collection.setName(name);
        collection.setDbName(this.dbName());
        this.client().createCollection(collection);
        this.reloadChild();
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
                        List<MongoCollection> collections = this.client().listCollections(this.dbName());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MongoCollection collection : collections) {
                                list.add(new MongoCollectionTreeItem(collection, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<MongoCollectionTreeItem> list = children;
                            List<MongoCollectionTreeItem> delList = new ArrayList<>();
                            List<MongoCollectionTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (MongoCollectionTreeItem item : list) {
                                if (collections.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (MongoCollection collection : collections) {
                                if (list.parallelStream().noneMatch(item -> collection.compare(item.value()))) {
                                    addList.add(new MongoCollectionTreeItem(collection, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (MongoCollectionTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    collections.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
                                }
                            }
                            list.removeAll(delList);
                            list.addAll(addList);
                        }
                        this.expend();
                    })
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex);
                    })
                    .onSuccess(this::refresh)
                    .onFinish(() -> this.setLoading(false))
                    .build();
            // 执行业务
            this.startWaiting(task);
        }
    }

    @Override
    public void reloadChild() {
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

    @Override
    public synchronized void doFilter(RichTreeItemFilter itemFilter) {
        super.doFilter(itemFilter);
        this.refresh();
    }

    public void addTable(MongoCollection table) {
        this.addChild(new MongoCollectionTreeItem(table, this.getTreeView()));
        this.sortChild(this.isSortAsc());
    }

    public long collectionsSize() {
        return this.parent().listCollectionNames().size();
    }

    private Integer collectionsSize;

    public Integer getCollectionsSize() {
        if (this.collectionsSize == null) {
            this.collectionsSize = Math.toIntExact(this.collectionsSize());
        }
        return this.collectionsSize;
    }
}
