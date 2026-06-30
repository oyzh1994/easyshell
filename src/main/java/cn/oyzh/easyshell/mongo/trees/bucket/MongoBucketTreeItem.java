package cn.oyzh.easyshell.mongo.trees.bucket;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.bucket.MongoBucket;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.MongoColumns;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.record.MongoRecordFilter;
import cn.oyzh.easyshell.mongo.record.MongoSelectRecordParam;
import cn.oyzh.easyshell.mongo.trees.MongoTreeItem;
import cn.oyzh.easyshell.mongo.trees.database.MongoDatabaseTreeItem;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import org.bson.types.ObjectId;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * db树表节点
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class MongoBucketTreeItem extends MongoTreeItem<MongoBucketTreeItemValue> {

    /**
     * 当前值
     */
    private final MongoBucket value;

    public MongoBucketTreeItem(MongoBucket table, RichTreeView treeView) {
        super(treeView);
        this.value = table;
        this.setValue(new MongoBucketTreeItemValue(this));
    }

    @Override
    public MongoBucketsTreeItem parent() {
        return (MongoBucketsTreeItem) super.parent();
    }

    public ShellMongoClient client() {
        return this.parent().client();
    }

    public String dbName() {
        return this.parent().dbName();
    }

    public String bucketName() {
        return this.value.getName();
    }

    /**
     * 获取redis信息
     *
     * @return redis信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem openBucket = MenuItemHelper.openBucket( this::onPrimaryDoubleClick);
        items.add(openBucket);
        FXMenuItem clearBucket = MenuItemHelper.clearBucket( this::clearBucket);
        items.add(clearBucket);
        FXMenuItem deleteBucket = MenuItemHelper.deleteBucket( this::delete);
        items.add(deleteBucket);
        return items;
    }

    /**
     * 清空集合
     */
    private void clearBucket() {
        if (MessageBox.confirm(I18nHelper.clearBucket() + "[" + this.bucketName() + "]")) {
            this.dbItem().clearBucket(this.bucketName());
            this.parent().reloadChild();
        }
    }

    @Override
    public void delete() {
        try {
            if (MessageBox.confirm(I18nHelper.deleteBucket() + "[" + this.bucketName() + "]")) {
                this.dbItem().dropBucket(this.bucketName());
                ShellMongoEventUtil.bucketDropped(this, this.dbItem());
                this.remove();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    public MongoDatabaseTreeItem dbItem() {
        if (this.parent() == null) {
            return null;
        }
        return this.parent().parent();
    }

    public String infoName() {
        return parent().infoName();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMongoEventUtil.bucketOpen(this, this.dbItem());
    }

    @Override
    public void loadChild() {
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    public MongoBucket value() {
        return value;
    }

    public MongoColumns bucketColumns() {
        return this.client().bucketColumns();
    }

    public Paging<MongoRecord> recordPage(long pageNo, long limit, List<MongoRecordFilter> filters, MongoColumns columns) {
        MongoSelectRecordParam param = new MongoSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setDbName(this.dbName());
        param.setStart(pageNo * limit);
        param.setCollectionName(this.bucketName());
        List<MongoRecord> rows = this.client().selectBucketRecords(param);
        long count = this.client().selectBucketRecordCount(param);
        Paging<MongoRecord> paging = new Paging<>(rows, limit, count);
        paging.currentPage(pageNo);
        return paging;
    }

    public ObjectId uploadRecord(File file) throws Exception {
        return this.client().uploadBucketRecord(this.dbName(), this.bucketName(), file);
    }

    public MongoRecord selectRecord(Object _id) {
        return this.client().selectBucketRecord(this.dbName(), this.bucketName(), _id);
    }

    public void downloadRecord(Object _id, String file) throws Exception {
        this.client().downloadBucketRecord(this.dbName(), this.bucketName(), _id, file);
    }

    public long deleteRecord(Object _id) {
        return this.client().deleteBucketRecord(this.dbName(), this.bucketName(), _id);
    }

    public long deleteRecord(MongoRecord record) {
        return this.deleteRecord(record._idValue());
    }

    public long updateRecord(MongoRecord record) {
        return this.client().updateBucketRecord( record);
    }

    public Object eval(String script) throws Exception {
        return this.client().eval(this.dbName(), script);
    }
}
