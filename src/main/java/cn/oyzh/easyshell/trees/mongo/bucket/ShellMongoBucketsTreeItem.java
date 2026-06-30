package cn.oyzh.easyshell.trees.mongo.bucket;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mongo.bucket.MongoBucket;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.collection.MongoCollection;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.collection.ShellMongoCollectionTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
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
 * mongodb树存储桶类型节点
 *
 * @author oyzh
 * @since 2023/12/08
 */
public class ShellMongoBucketsTreeItem extends ShellMongoTreeItem<ShellMongoBucketsTreeItemValue> {

    public ShellMongoBucketsTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMongoBucketsTreeItemValue(this));
        super.unfilteredChildren().addListener((ListChangeListener<TreeItem<?>>) change -> {
            this.bucketsSize = null;
        });
    }

    @Override
    public ShellMongoDatabaseTreeItem parent() {
        return (ShellMongoDatabaseTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem reload = MenuItemHelper.reloadData( this::reloadChild);
        FXMenuItem add = MenuItemHelper.addBucket( this::addBucket);
        items.add(add);
        items.add(reload);
        return items;
    }

    private void addBucket() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputBucketName());
        if (StringUtil.isBlank(name)) {
            return;
        }
        this.client().createBucket(this.dbName(), name);
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
                        List<MongoBucket> buckets = this.client().listBuckets(this.dbName());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MongoBucket bucket : buckets) {
                                list.add(new ShellMongoBucketTreeItem(bucket, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellMongoBucketTreeItem> list = children;
                            List<ShellMongoBucketTreeItem> delList = new ArrayList<>();
                            List<ShellMongoBucketTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellMongoBucketTreeItem item : list) {
                                if (buckets.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (MongoBucket bucket : buckets) {
                                if (list.parallelStream().noneMatch(item -> bucket.compare(item.value()))) {
                                    addList.add(new ShellMongoBucketTreeItem(bucket, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellMongoBucketTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    buckets.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
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
        this.addChild(new ShellMongoCollectionTreeItem(table, this.getTreeView()));
        this.sortChild(this.isSortAsc());
    }

    public long bucketsSize() {
       return this.parent().listBucketNames().size();
    }

    private Integer bucketsSize;

    public Integer getBucketsSize() {
        if (this.bucketsSize == null) {
            this.bucketsSize = Math.toIntExact(this.bucketsSize());
        }
        return this.bucketsSize;
    }
}
