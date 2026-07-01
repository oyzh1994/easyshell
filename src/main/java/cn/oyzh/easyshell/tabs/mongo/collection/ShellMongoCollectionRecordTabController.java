package cn.oyzh.easyshell.tabs.mongo.collection;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.DBObjectList;
import cn.oyzh.easyshell.data.db.listener.DBStatusListener;
import cn.oyzh.easyshell.data.db.listener.DBStatusListenerManager;
import cn.oyzh.easyshell.data.db.ui.DBStatusColumn;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.mongo.ShellMongoRecordColumn;
import cn.oyzh.easyshell.fx.mongo.ShellMongoRecordTableView;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.record.MongoRecordFilter;
import cn.oyzh.easyshell.popups.mongo.ShellMongoPageSettingPopupController;
import cn.oyzh.easyshell.popups.mongo.ShellMongoRecordFilterPopupController;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.trees.mongo.collection.ShellMongoCollectionTreeItem;
import cn.oyzh.easyshell.util.mongo.ShellMongoDataUtil;
import cn.oyzh.easyshell.util.mongo.ShellMongoRecordUtil;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewFactory;
import cn.oyzh.fx.gui.page.PageBox;
import cn.oyzh.fx.gui.page.PageEvent;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import org.bson.BsonValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 集合tab内容组件
 *
 * @author oyzh
 * @since 2023/12/24
 */
public class ShellMongoCollectionRecordTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * 集合节点
     */
    private ObjectProperty<ShellMongoCollectionTreeItem> itemProperty;

    /**
     * 分页数据
     */
    private Paging<MongoRecord> pageData;

    /**
     * 记录过滤按钮
     */
    @FXML
    private SVGGlyph filter;

    /**
     * 数据分页组件
     */
    @FXML
    private PageBox<MongoRecord> pageBox;

    /**
     * 数据表单组件
     */
    @FXML
    private ShellMongoRecordTableView recordTable;

    /**
     * 过滤列表
     */
    private List<MongoRecordFilter> filters;

    /**
     * 应用
     */
    @FXML
    private SVGGlyph apply;

    /**
     * 抛弃
     */
    @FXML
    private SVGGlyph discard;

    /**
     * 记录变更监听器
     */
    private DBStatusListener changeListener;

    /**
     * 字段列表
     */
    private MongoColumns columns;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 执行初始化
     *
     * @param item 集合节点
     */
    public void init(ShellMongoCollectionTreeItem item) {
        this.itemProperty = new SimpleObjectProperty<>(item);
        this.itemProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                this.closeTab();
            }
        });
        item.parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                this.closeTab();
            }
        });
        this.reload();
        if (this.changeListener == null) {
            this.changeListener = new DBStatusListener(this.getItem().dbName() + ":" + this.getItem().collectionName()) {
                @Override
                public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                    apply.enable();
                }
            };
        }
    }

    public ShellMongoCollectionTreeItem getItem() {
        return this.itemProperty.get();
    }

    /**
     * 初始化数据列表
     *
     * @param pageNo 数据页码
     */
    private void initDataList(long pageNo) {
        try {
            this.pageData = this.getItem().recordPage(pageNo, this.setting.getMongoRecordPageLimit(), this.enabledFilters(), this.columns);
            this.pageBox.setPaging(this.pageData);
            List<MongoRecord> records = this.pageData.dataList();
            // 更新字段
            this.updateColumns(records);
            // 初始化数据
            this.initRecords(records);
            // 纠正记录
            this.correctRecords();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 初始化数据列表，带遮罩板
     *
     * @param pageNo 数据页码
     */
    private void initDataListByMask(long pageNo) {
        StageManager.showMask(() -> this.initDataList(pageNo));
    }

    /**
     * 获取已启用的表过滤条件
     *
     * @return 已启用的表过滤条件
     */
    private List<MongoRecordFilter> enabledFilters() {
        if (CollectionUtil.isNotEmpty(this.filters)) {
            return this.filters.stream().filter(MongoRecordFilter::isEnabled).toList();
        }
        return null;
    }

    /**
     * 更新字段
     *
     * @param records 记录列表
     */
    private void updateColumns(List<MongoRecord> records) {
        boolean changed = false;
        MongoColumns columnList = this.columns;
        if (columnList == null) {
            columnList = new MongoColumns();
            changed = true;
        }

        Set<String> colNames = new HashSet<>();
        for (MongoRecord record : records) {
            MongoColumns mongoColumns = record.getColumns();
            for (MongoColumn mongoColumn : mongoColumns) {
                MongoColumn column = columnList.column(mongoColumn.getName());
                if (column == null) {
                    column = new MongoColumn();
                    column.copy(mongoColumn);
                    columnList.add(column);
                    changed = true;
                }
                colNames.add(mongoColumn.getName());
            }
        }

        List<MongoColumn> delList = new ArrayList<>();
        for (MongoColumn column : columnList) {
            if (!colNames.contains(column.getName())) {
                delList.add(column);
                changed = true;
            }
        }

        columnList.removeAll(delList);

        if (changed) {
            this.initColumns(columnList);
        }
    }

    /**
     * 初始化计数
     *
     * @param count 计数
     */
    private void initCount(long count) {
        this.pageData = new Paging<>(this.recordTable.itemList(), this.pageData.limit(), count);
        this.pageBox.setPaging(this.pageData);
    }

    /**
     * 初始化列
     *
     * @param columns 列数据
     */
    private void initColumns(MongoColumns columns) {
        // 设置字段列表
        this.columns = columns;
        if (this.columns == null) {
            this.recordTable.clearColumn();
            return;
        }
        // 数据列集合
        List<FXTableColumn<MongoRecord, Object>> columnList = new ArrayList<>();
        DBStatusColumn<MongoRecord> statusColumn = new DBStatusColumn<>();
        columnList.add(statusColumn);
        for (MongoColumn column : columns) {
            ShellMongoRecordColumn tableColumn = new ShellMongoRecordColumn(column);
            tableColumn.setPrefWidth(ShellMongoRecordUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        this.recordTable.setColumn(columnList);
    }

    /**
     * 初始化记录
     *
     * @param records 数据
     */
    private void initRecords(List<MongoRecord> records) {
        this.recordTable.setItem(records);
    }

    /**
     * 初始化记录
     *
     */
    private void correctRecords() {
        List<MongoRecord> records = this.recordTable.getItems();
        for (MongoRecord record : records) {
            record.correctColumns(this.columns);
        }
    }

    /**
     * 添加记录
     */
    @FXML
    private void addRecord() {
        try {
            MongoRecord lastItem = (MongoRecord) this.recordTable.lastItem();
            if (lastItem == null) {
                this.addDocument();
            } else {
                MongoColumns columns = new MongoColumns(lastItem.getColumns());
                MongoRecord record = new MongoRecord(columns);
                record.setCreated(true);
                for (MongoColumn column : columns) {
                    record.putValue(column, column.defaultValue());
                }
                this.recordTable.addItem(record);
                this.recordTable.selectLast();
                // 初始化计数
                this.initCount(this.pageData.count() + 1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 添加文档
     */
    @FXML
    private void addDocument() {
        try {
            StageAdapter adapter = ShellMongoViewFactory.documentAdd(this.columns);
            if (adapter == null) {
                return;
            }
            String doc = adapter.getProp("doc");
            if (StringUtil.isBlank(doc)) {
                return;
            }
            // 转换为脚本
            String script = ShellMongoDataUtil.toInsertScript(this.getItem().collectionName(), doc);
            // 更新数据
            InsertOneResult result = (InsertOneResult) this.getItem().eval(script);
            if (result == null || result.getInsertedId() == null) {
                MessageBox.warn(I18nHelper.addDocumentFail());
                return;
            }
            // 刷新记录
            if (this.recordTable.isItemEmpty()) {
                this.apply.disable();
                this.reload();
            } else {// 更新记录
                BsonValue _id = result.getInsertedId();
                MongoRecord record = this.getItem().selectCollectionRecord(_id);
                if (record == null) {
                    MessageBox.warn(I18nHelper.addDocumentFail());
                    return;
                }
                // 更新字段
                List<MongoRecord> list = new ArrayList<>(this.recordTable.getItems());
                list.add(record);
                // 更新字段
                this.updateColumns(list);
                // 追加内容
                this.recordTable.addItem(record);
                this.recordTable.selectLast();
                // 纠正记录
                this.correctRecords();
                this.apply.disable();
                // 初始化计数
                this.initCount(this.pageData.count() + 1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 编辑文档
     */
    @FXML
    private void editDocument() {
        try {
            MongoRecord record = this.recordTable.getSelectedItem();
            if (record == null) {
                return;
            }
            StageAdapter adapter = ShellMongoViewFactory.documentUpdate(record);
            if (adapter == null) {
                return;
            }
            String doc = adapter.getProp("doc");
            if (doc == null) {
                return;
            }
            Object id = record._idValue();
            // 转换为脚本
            String script = ShellMongoDataUtil.toUpdateScript(this.getItem().collectionName(), id, doc);
            // 查询数据
            UpdateResult result = (UpdateResult) this.getItem().eval(script);
            if (result.getMatchedCount() != 1) {
                MessageBox.warn(I18nHelper.addDocumentFail());
            }
            // 查询数据
            MongoRecord r = this.getItem().selectCollectionRecord(id);
            if (r == null) {
                MessageBox.warn(I18nHelper.addDocumentFail());
                return;
            }
            // 复制数据
            record.copy(r);
            // 清除状态
            record.clearStatus();
            // 更新字段
            this.updateColumns(this.recordTable.getItems());
            // 纠正记录
            this.correctRecords();
            this.apply.disable();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 插入记录
     *
     * @param record 记录
     */
    private void insertRecord(MongoRecord record) {
        BsonValue _id = this.getItem().insertRecord(record);
        record.set_id(_id);
    }

    /**
     * 更改记录
     *
     * @param record 记录
     */
    private void updateRecord(MongoRecord record) {
        // 更新行
        long result = this.getItem().updateRecord(record);
        // 更新字段
        if (result == 1) {
            // 更新字段
            this.updateColumns(this.recordTable.getItems());
            // 纠正记录
            this.correctRecords();
        } else {// 操作失败
            MessageBox.warn(I18nHelper.updateDocumentFail());
        }
    }

    /**
     * 应用变更
     */
    @FXML
    private void apply() {
        if (this.apply.isEnable()) {
            try {
                List<MongoRecord> records = this.recordTable.getItems();
                for (MongoRecord record : records) {
                    if (DBObjectList.isCreated(record)) {
                        this.insertRecord(record);
                        record.clearStatus();
                    } else if (DBObjectList.isChanged(record)) {
                        this.updateRecord(record);
                        record.clearStatus();
                    }
                }
                this.apply.disable();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        }
    }

    /**
     * 丢弃变更
     */
    @FXML
    private void discard() {
        try {
            MongoRecord discardRecord = null;
            for (MongoRecord record : this.recordTable.getItems()) {
                if (record.isCreated()) {
                    discardRecord = record;
                } else if (record.isChanged()) {
                    record.discard();
                }
            }
            this.recordTable.removeItem(discardRecord);
            this.apply.disable();
            this.initCount(this.recordTable.getItemSize());
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 刷新记录
     */
    @FXML
    public void reload() {
        StageManager.showMask(this::doReload);
    }

    /**
     * 刷新记录，实际业务
     */
    private void doReload() {
        try {
            // 检查是否有未保存的数据
            if (this.apply.isEnable() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
                return;
            }
            // 初始化数据
            this.initDataListByMask(0);
            // 设置过滤激活
            this.filter.setActive(CollectionUtil.isNotEmpty(this.enabledFilters()));
            // 禁用组件
            this.apply.disable();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 过滤记录
     */
    @FXML
    private void filter() {
        try {
            PopupAdapter popup = PopupManager.parsePopup(ShellMongoRecordFilterPopupController.class);
            popup.setProp("item", this.getItem());
            popup.setProp("filters", this.filters);
            popup.setProp("columns", this.columns);
            popup.showPopup(this.filter);
            popup.setSubmitHandler(filters -> {
                this.setFilters((List<MongoRecordFilter>) filters);
                this.reload();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 下一页
     */
    @FXML
    private void nextPage() {
        this.initDataListByMask(this.pageData.nextPage());
    }

    /**
     * 上一页
     */
    @FXML
    private void prevPage() {
        this.initDataListByMask(this.pageData.prevPage());
    }

    /**
     * 尾页
     */
    @FXML
    private void lastPage() {
        this.initDataListByMask(this.pageData.lastPage());
    }

    /**
     * 首页
     */
    @FXML
    private void firstPage() {
        this.initDataListByMask(0);
    }

    /**
     * 跳页
     */
    @FXML
    private void pageJump(PageEvent.PageJumpEvent event) {
        this.initDataListByMask(event.getPage());
    }

    /**
     * 页码设置
     */
    @FXML
    private void pageSetting() {
        PopupAdapter popup = PopupManager.parsePopup(ShellMongoPageSettingPopupController.class);
        popup.showPopup(this.pageBox.getSettingBtn());
        int limit = this.setting.getMongoRecordPageLimit();
        popup.setSubmitHandler(o -> {
            if (o instanceof Integer l && l != limit) {
                this.firstPage();
            }
        });
    }

    /**
     * 删除记录
     */
    @FXML
    private void deleteRecord() {
        if (!MessageBox.confirm(I18nHelper.deleteDocument() + "?")) {
            return;
        }
        List<MongoRecord> records = new ArrayList<>(this.recordTable.getSelectedItems());
        StageManager.showMask(() -> this.deleteRecords(records));
    }

    /**
     * 删除记录
     *
     * @param records 记录
     */
    private void deleteRecords(List<MongoRecord> records) {
        try {
            boolean success = false;
            for (MongoRecord record : records) {
                success = this.deleteRecord(record);
                if (!success) {
                    break;
                }
            }
            // 操作成功
            if (success) {
                this.recordTable.removeItem(records);
                this.initCount(this.pageData.count() - records.size());
            } else {// 操作失败
                MessageBox.warnToast(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 删除记录
     *
     * @param record 记录
     * @return 结果
     */
    private boolean deleteRecord(MongoRecord record) {
        boolean success;
        // 如果是新增的数据，直接删除
        if (record.isCreated()) {
            success = true;
        } else {
            success = this.getItem().deleteRecord(record) == 1;
        }
        // 操作成功
        if (success) {
            this.recordTable.removeItem(record);
        } else {// 操作失败
            MessageBox.warnToast(I18nHelper.operationFail());
        }
        return success;
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        DBStatusListenerManager.removeListener(this.changeListener);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.discard.disableProperty().bind(this.apply.disableProperty());
        this.apply.disabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                NodeGroupUtil.enable(this.root, "action2");
            } else {
                NodeGroupUtil.disable(this.root, "action2");
            }
        });
        this.recordTable.getItems().addListener((ListChangeListener<MongoRecord>) c -> {
            if (c.next() && c.wasAdded()) {
                List<? extends MongoRecord> rows = c.getAddedSubList();
                for (MongoRecord row : rows) {
                    if (DBObjectList.isCreated(row)) {
                        this.apply.enable();
                        break;
                    }
                }
            }
        });
        this.recordTable.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.setEditable(true);
            }
            this.recordTable.refresh();
        });
        this.recordTable.setCtrlSAction(this::apply);
        NodeUtil.nodeOnCtrlS(this.root, this::apply);
    }

    public List<MongoRecordFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<MongoRecordFilter> filters) {
        this.filters = filters;
    }

    /**
     * 导入数据
     */
    @FXML
    private void importData() {
        ShellMongoViewFactory.importData(this.getItem().client(), this.getItem().dbName());
    }

    /**
     * 导出数据
     */
    @FXML
    private void exportData() {
        ShellMongoViewFactory.exportData(this.getItem().client(), this.getItem().dbName(), this.getItem().collectionName());
    }
}
