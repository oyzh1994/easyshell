package cn.oyzh.easyshell.trees.mongo.database;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.MongoDatabase;
import cn.oyzh.easyshell.mongo.MongoFunction;
import cn.oyzh.easyshell.mongo.MongoRecord;
import cn.oyzh.easyshell.query.mongo.MongoExecuteResult;
import cn.oyzh.easyshell.query.mongo.MongoQueryResults;
import cn.oyzh.easyshell.trees.mongo.MongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.bucket.MongoBucketsTreeItem;
import cn.oyzh.easyshell.trees.mongo.collection.MongoCollectionsTreeItem;
import cn.oyzh.easyshell.trees.mongo.function.ShellMongoFunctionsTreeItem;
import cn.oyzh.easyshell.trees.mongo.query.MongoQueriesTreeItem;
import cn.oyzh.easyshell.trees.mongo.root.ShellMongoRootTreeItem;
import cn.oyzh.easyshell.trees.mongo.terminal.MongoTerminalTreeItem;
import cn.oyzh.easyshell.util.mongo.MongoViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import org.bson.BsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * db树database节点
 *
 * @author oyzh
 * @since 2023/12/12
 */
public class MongoDatabaseTreeItem extends MongoTreeItem<MongoDatabaseTreeItemValue> {

    /**
     * 当前值
     */
    private final MongoDatabase value;

    public MongoDatabase value() {
        return value;
    }

    public MongoDatabaseTreeItem(MongoDatabase database, RichTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(true);
        this.value = database;
        this.setValue(new MongoDatabaseTreeItemValue(this));
    }

    @Override
    public ShellMongoRootTreeItem parent() {
        return (ShellMongoRootTreeItem) super.parent();
    }

    public String dbName() {
        return this.value.getName();
    }

    public String userName() {
        return this.info().getUser();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        if (!this.isChildEmpty()) {
            FXMenuItem closeDB = MenuItemHelper.closeDatabase(this::closeDB);
            items.add(closeDB);
        }
        FXMenuItem dropDB = MenuItemHelper.deleteDatabase(this::delete);
        items.add(dropDB);
        FXMenuItem dumpData = MenuItemHelper.dumpData(this::dump);
        items.add(dumpData);
        FXMenuItem runScriptFile = MenuItemHelper.runScriptFile(this::runScriptFile);
        items.add(runScriptFile);
        return items;
    }

    /**
     * 转储
     */
    private void dump() {
        MongoViewFactory.dumpData(this.client(), this.dbName(), null, 1);
    }

    /**
     * 转储
     */
    private void runScriptFile() {
        MongoViewFactory.runScriptFile(this.client(), this.dbName());
    }

    @Override
    public void delete() {
        Task task = TaskBuilder.newBuilder()
                .onStart(() -> {
                    if (MessageBox.confirm(I18nHelper.deleteDatabase() + "[" + this.dbName() + "]")) {
                        if (this.parent().dropDatabase(this.dbName())) {
                            ShellMongoEventUtil.databaseDropped(this);
                            super.remove();
                        } else {
                            MessageBox.warn(I18nHelper.operationFail());
                        }
                    }
                })
                .onSuccess(super::refresh)
                .build();
        super.startWaiting(task);
    }

    //    /**
    //     * 编辑数据库
    //     */
    //    public void editDB() {
    //        StageAdapter fxView = StageManager.parseStage(MongoDatabaseUpdateController.class, this.window());
    //        fxView.setProp("database", this.value);
    //        fxView.setProp("connectItem", this.parent());
    //        fxView.display();
    //    }

    /**
     * 关闭数据库
     */
    public void closeDB() {
        this.clearChild();
        this.collapse();
        this.setLoaded(false);
        ShellMongoEventUtil.databaseClosed(this);
    }

    @Override
    public void loadChild() {
        if (!this.isLoading() && !this.isLoaded()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<TreeItem<?>> typeItems = new ArrayList<>();
                        typeItems.add(new MongoCollectionsTreeItem(this.getTreeView()));
                        typeItems.add(new MongoBucketsTreeItem(this.getTreeView()));
                        typeItems.add(new ShellMongoFunctionsTreeItem(this.getTreeView()));
                        typeItems.add(new MongoQueriesTreeItem(this.getTreeView()));
                        typeItems.add(new MongoTerminalTreeItem(this.getTreeView()));
                        super.setChild(typeItems);
                    })
                    .onSuccess(this::expend)
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.error(ex.getMessage());
                    })
                    .onFinish(() -> this.setLoading(false))
                    .build();
            super.startWaiting(task);
        }

    }

    /**
     * 获取查询类型子节点
     *
     * @return 查询类型子节点
     */
    public MongoQueriesTreeItem getQueryTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof MongoQueriesTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * 获取函数类型子节点
     *
     * @return 函数类型子节点
     */
    public ShellMongoFunctionsTreeItem getFunctionTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellMongoFunctionsTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * 获取db客户端
     *
     * @return db客户端
     */
    public ShellMongoClient client() {
        return this.parent().getClient();
    }

    /**
     * 获取db信息
     *
     * @return db信息
     */
    public ShellConnect info() {
        return this.parent().connect();
    }

    public String infoName() {
        return this.info().getName();
    }

    public String connectName() {
        return this.info().getName();
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
    public boolean itemVisible() {
        return this.isVisible();
    }

    @Override
    public synchronized void doFilter(RichTreeItemFilter itemFilter) {
        super.doFilter(itemFilter);
        this.refresh();
    }

    public ShellConnect shellConnect() {
        return this.client().getShellConnect();
    }

    public void dropCollection(String collectionName) {
        this.client().dropCollection(this.dbName(), collectionName);
    }

    public void clearCollection(String collectionName) {
        this.client().clearCollection(this.dbName(), collectionName);
    }

    public void dropBucket(String bucketName) {
        this.client().dropBucket(this.dbName(), bucketName);
    }

    public void clearBucket(String bucketName) {
        this.client().clearBucket(this.dbName(), bucketName);
    }

    public MongoExecuteResult executeSingleScript(String script) throws Exception {
        return this.client().executeSingleScript(this.dbName(), script);
    }

    public MongoQueryResults<MongoExecuteResult> executeScript(String script) {
        return this.client().executeScript(this.dbName(), script);
    }

    public long deleteCollectionRecord(MongoRecord record) {
        return this.client().deleteCollectionRecord(record);
    }

    public long updateCollectionRecord(MongoRecord record) {
        return this.client().updateCollectionRecord(record);
    }

    public BsonValue insertCollectionRecord(MongoRecord record) {
        return this.client().insertCollectionRecord(record);
    }

    public MongoRecord selectCollectionRecord(String collectionName, Object id) {
        return this.client().selectCollectionRecord(this.dbName(), collectionName, id);
    }

    public void dropFunction(MongoFunction value) {
        this.client().dropFunction(this.dbName(), value.getName());
    }

    public void renameFunction(String oldName, String newName) {
        this.client().renameFunction(this.dbName(), oldName, newName);
    }

    public MongoFunction selectFunction(String functionName) {
        return this.client().selectFunction(this.dbName(), functionName);
    }

    public void createFunction(MongoFunction function) {
        this.client().createFunction(this.dbName(), function.getName(), function.getCode());
    }

    public void alertFunction(MongoFunction function) {
        this.client().alertFunction(this.dbName(), function.getName(), function.getCode());
    }

    public void renameCollection(String oldName, String newName) {
        this.client().renameCollection(this.dbName(), oldName, newName);
    }

    public List<String> listCollectionNames() {
        return this.client().listCollectionNames(this.dbName());
    }

    public List<String> listBucketNames() {
        return this.client().listBucketNames(this.dbName());
    }

    public Object eval(String script) throws Exception {
        return this.client().eval(this.dbName(), script);
    }
}
