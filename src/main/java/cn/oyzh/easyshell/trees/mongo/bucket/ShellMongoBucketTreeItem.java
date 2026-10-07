package cn.oyzh.easyshell.trees.mongo.bucket;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.bucket.MongoBucket;
import cn.oyzh.easyshell.mongo.bucket.MongoBucketFile;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
import cn.oyzh.easyshell.mongo.record.MongoRecordFilter;
import cn.oyzh.easyshell.mongo.record.MongoSelectRecordParam;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
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
 * mongodb树存储桶节点
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoBucketTreeItem extends ShellMongoTreeItem<ShellMongoBucketTreeItemValue> {

    /**
     * 当前值
     */
    private final MongoBucket value;

    /**
     * 构造存储桶节点
     *
     * @param table    存储桶对象
     * @param treeView 树视图
     */
    public ShellMongoBucketTreeItem(MongoBucket table, RichTreeView treeView) {
        super(treeView);
        this.value = table;
        this.setValue(new ShellMongoBucketTreeItemValue(this));
    }

    @Override
    public ShellMongoBucketsTreeItem parent() {
        return (ShellMongoBucketsTreeItem) super.parent();
    }

    /**
     * 获取mongodb客户端
     *
     * @return mongodb客户端
     */
    public ShellMongoClient client() {
        return this.parent().client();
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
     * 获取存储桶名称
     *
     * @return 存储桶名称
     */
    public String bucketName() {
        return this.value.getName();
    }

    /**
     * 获取mongodb信息
     *
     * @return mongodb信息
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
                this.parent().clearBucketsSize();
                this.remove();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 获取所属数据库节点
     *
     * @return 数据库节点
     */
    public ShellMongoDatabaseTreeItem dbItem() {
        if (this.parent() == null) {
            return null;
        }
        return this.parent().parent();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String connectName() {
        return parent().connectName();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMongoEventUtil.bucketOpen(this, this.dbItem());
    }
//
//    @Override
//    public void loadChild() {
//    }
//
//    @Override
//    public void reloadChild() {
//        this.clearChild();
//        this.setLoaded(false);
//        this.loadChild();
//    }

    /**
     * 获取存储桶对象
     *
     * @return 存储桶对象
     */
    public MongoBucket value() {
        return value;
    }

//    public MongoColumns bucketColumns() {
//        return this.client().bucketColumns();
//    }

    /**
     * 分页查询存储桶文件
     *
     * @param pageNo  页码
     * @param limit   每页大小
     * @param filters 过滤条件
     * @param columns 列信息
     * @return 分页结果
     */
    public Paging<MongoBucketFile> recordPage(long pageNo, long limit, List<MongoRecordFilter> filters, MongoColumns columns) {
        MongoSelectRecordParam param = new MongoSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setDbName(this.dbName());
        param.setStart(pageNo * limit);
        param.setCollectionName(this.bucketName());
        List<MongoBucketFile> rows = this.client().selectBucketRecords(param);
        long count = this.client().selectBucketRecordCount(param);
        Paging<MongoBucketFile> paging = new Paging<>(rows, limit, count);
        paging.currentPage(pageNo);
        return paging;
    }

    /**
     * 上传文件到存储桶
     *
     * @param file 文件
     * @return 文件主键
     * @throws Exception 异常
     */
    public ObjectId uploadRecord(File file) throws Exception {
        return this.client().uploadBucketRecord(this.dbName(), this.bucketName(), file);
    }

    /**
     * 查询存储桶文件
     *
     * @param _id 文件主键
     * @return 存储桶文件
     */
    public MongoBucketFile selectRecord(Object _id) {
        return this.client().selectBucketRecord(this.dbName(), this.bucketName(), _id);
    }

    /**
     * 下载存储桶文件
     *
     * @param _id  文件主键
     * @param file 目标文件
     * @throws Exception 异常
     */
    public void downloadRecord(Object _id, String file) throws Exception {
        this.client().downloadBucketRecord(this.dbName(), this.bucketName(), _id, file);
    }

    /**
     * 删除存储桶文件
     *
     * @param _id 文件主键
     * @return 受影响行数
     */
    public long deleteRecord(Object _id) {
        return this.client().deleteBucketRecord(this.dbName(), this.bucketName(), _id);
    }

//    public long deleteRecord(MongoRecord record) {
//        return this.deleteRecord(record._idValue());
//    }

//    public long updateRecord(MongoRecord record) {
//        return this.client().updateBucketRecord( record);
//    }

    /**
     * 更新存储桶文件
     *
     * @param record 存储桶文件
     * @return 受影响行数
     */
    public long updateRecord(MongoBucketFile record) {
        return this.client().updateBucketRecord( record);
    }

    /**
     * 执行脚本
     *
     * @param script 脚本
     * @return 执行结果
     * @throws Exception 异常
     */
    public Object eval(String script) throws Exception {
        return this.client().eval(this.dbName(), script);
    }
}
