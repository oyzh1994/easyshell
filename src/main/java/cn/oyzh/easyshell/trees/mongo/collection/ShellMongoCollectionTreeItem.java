package cn.oyzh.easyshell.trees.mongo.collection;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.collection.MongoCollection;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.record.MongoRecordFilter;
import cn.oyzh.easyshell.mongo.record.MongoSelectRecordParam;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;
import org.bson.BsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * mongodb树集合节点
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoCollectionTreeItem extends ShellMongoTreeItem<ShellMongoCollectionTreeItemValue> {

    /**
     * 当前值
     */
    private final MongoCollection value;

    /**
     * 构造集合节点
     *
     * @param table    集合对象
     * @param treeView 树视图
     */
    public ShellMongoCollectionTreeItem(MongoCollection table, RichTreeView treeView) {
        super(treeView);
        this.value = table;
        this.setValue(new ShellMongoCollectionTreeItemValue(this));
    }

    @Override
    public ShellMongoCollectionsTreeItem parent() {
        return (ShellMongoCollectionsTreeItem) super.parent();
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
     * 获取集合名称
     *
     * @return 集合名称
     */
    public String collectionName() {
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
        FXMenuItem openCollection = MenuItemHelper.openCollection( this::onPrimaryDoubleClick);
        items.add(openCollection);
        FXMenuItem renameCollection = MenuItemHelper.renameCollection( this::rename);
        items.add(renameCollection);
        FXMenuItem clearCollection = MenuItemHelper.clearCollection( this::clearCollection);
        items.add(clearCollection);
        FXMenuItem deleteCollection = MenuItemHelper.deleteCollection( this::delete);
        items.add(deleteCollection);
        items.add(MenuItemHelper.separator());
        FXMenuItem dumpTable = MenuItemHelper.dumpData( this::dump);
        items.add(dumpTable);
        FXMenuItem exportTable = MenuItemHelper.exportData( this::export);
        items.add(exportTable);
        return items;
    }

    /**
     * 转储
     */
    private void dump() {
        ShellMongoViewFactory.dumpData(this.client(), this.dbName(), this.collectionName(), 1);
    }

    /**
     * 导出
     */
    private void export() {
        ShellMongoViewFactory.exportData(this.client(), this.dbName(), this.collectionName());
    }

    /**
     * 清空集合
     */
    private void clearCollection() {
        if (MessageBox.confirm(I18nHelper.clearCollection() + "[" + this.collectionName() + "]")) {
            this.dbItem().clearCollection(this.collectionName());
            this.parent().reloadChild();
        }
    }

    @Override
    public void delete() {
        try {
            if (MessageBox.confirm(I18nHelper.deleteCollection() + "[" + this.collectionName() + "]")) {
                this.dbItem().dropCollection(this.collectionName());
                ShellMongoEventUtil.collectionDropped(this, this.dbItem());
                this.parent().clearCollectionSize();
                this.remove();
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    public void rename() {
        try {
            String collectionName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.value.getName());
            // 名称为null或者跟当前名称相同，则忽略
            if (collectionName == null || Objects.equals(collectionName, this.value.getName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(collectionName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent());
                return;
            }
            String oldName = this.value.getName();
            // 修改名称
            this.dbItem().renameCollection(oldName, collectionName);
            this.value.setName(collectionName);
            this.refresh();
            ShellMongoEventUtil.collectionRenamed(oldName, collectionName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
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
        ShellMongoEventUtil.collectionOpen(this, this.dbItem());
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    /**
     * 获取集合对象
     *
     * @return 集合对象
     */
    public MongoCollection value() {
        return value;
    }

    /**
     * 分页查询集合记录
     *
     * @param pageNo  页码
     * @param limit   每页大小
     * @param filters 过滤条件
     * @param columns 列信息
     * @return 分页结果
     */
    public Paging<MongoRecord> recordPage(long pageNo, long limit, List<MongoRecordFilter> filters, MongoColumns columns) {
        MongoSelectRecordParam param = new MongoSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setDbName(this.dbName());
        param.setStart(pageNo * limit);
        param.setCollectionName(this.collectionName());
        List<MongoRecord> rows = this.client().selectCollectionRecords(param);
        long count = this.client().selectCollectionRecordCount(param);
        Paging<MongoRecord> paging = new Paging<>(rows, limit, count);
        paging.currentPage(pageNo);
        return paging;
    }

    /**
     * 新增集合记录
     *
     * @param record 记录
     * @return 主键
     */
    public BsonValue insertRecord(MongoRecord record) {
        return this.dbItem().insertCollectionRecord(record);
    }

    /**
     * 删除集合记录
     *
     * @param record 记录
     * @return 受影响行数
     */
    public long deleteRecord(MongoRecord record) {
        return this.dbItem().deleteCollectionRecord(record);
    }

    /**
     * 更新集合记录
     *
     * @param record 记录
     * @return 受影响行数
     */
    public long updateRecord(MongoRecord record) {
        return this.dbItem().updateCollectionRecord(record);
    }

    /**
     * 执行脚本
     *
     * @param script 脚本
     * @return 执行结果
     * @throws Exception 异常
     */
    public Object eval(String script) throws Exception {
        return this.dbItem().eval(script);
    }

    /**
     * 查询单条集合记录
     *
     * @param id 主键
     * @return 集合记录
     */
    public MongoRecord selectCollectionRecord(Object id) {
        return this.dbItem().selectCollectionRecord(this.collectionName(), id);
    }
}
