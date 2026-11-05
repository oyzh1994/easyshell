package cn.oyzh.easyshell.tabs.sqlite.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.sqlite.record.ShellSqliteRecordTableView;
import cn.oyzh.easyshell.popups.db.ShellDBPageSettingPopupController;
import cn.oyzh.easyshell.popups.sqlite.ShellSqliteRecordFilterPopupController;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordFilter;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordPrimaryKey;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewTreeItem;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteViewFactory;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.gui.page.PageBox;
import cn.oyzh.fx.gui.page.PageEvent;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 视图记录标签页控制器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteViewRecordTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * 数据库视图树节点
     */
    private ObjectProperty<ShellSqliteViewTreeItem> itemProperty;

    /**
     * 分页数据
     */
    private Paging<SQLiteRecord> pageData;

    /**
     * 记录过滤按钮
     */
    @FXML
    private SVGGlyph filter;

    /**
     * 缺少主键警告
     */
    @FXML
    private SVGGlyph missPrimaryKey;

    /**
     * 数据分页组件
     */
    @FXML
    private PageBox<SQLiteRecord> pageBox;

    /**
     * 数据表单组件
     */
    @FXML
    private ShellSqliteRecordTableView recordTable;

    /**
     * 过滤列表
     */
    private List<SQLiteRecordFilter> filters;

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
    private SQLiteColumns columns;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 执行初始化
     *
     * @param item 数据库视图树节点
     */
    public void init(ShellSqliteViewTreeItem item) {
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
        if (item.isUpdatable()) {
            if (this.changeListener == null) {
                this.changeListener = new DBStatusListener(this.getItem().dbName() + ":" + this.getItem().viewName()) {
                    @Override
                    public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                        apply.enable();
                    }
                };
            }
            // 部分按钮显示处理
            this.apply.display();
            NodeGroupUtil.display(this.getTab(), "action2");
        }
    }

    /**
     * 获取树节点
     *
     * @return 树节点
     */
    public ShellSqliteViewTreeItem getItem() {
        return this.itemProperty.get();
    }

    /**
     * 初始化数据列表
     *
     * @param pageNo 数据页码
     */
    private void initDataList(long pageNo) {
        try {
            this.pageData = this.getItem().recordPage(pageNo, this.setting.getRecordPageLimit(),
                    this.enabledFilters(), this.columns);
            this.pageBox.setPaging(this.pageData);
            this.initRecords(this.pageData.dataList());
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 初始化数据列表，带遮罩板
     *
     * @param pageNo 数据页码
     */
    private void initDataListByMask(long pageNo) {
        StageManager.showMask(this.getWindow(), () -> this.initDataList(pageNo));
    }

    /**
     * 获取已启用的表过滤条件
     *
     * @return 已启用的表过滤条件
     */
    private List<SQLiteRecordFilter> enabledFilters() {
        if (CollectionUtil.isNotEmpty(this.filters)) {
            return this.filters.stream().filter(SQLiteRecordFilter::isEnabled).toList();
        }
        return null;
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
    private void initColumns(SQLiteColumns columns) {
        this.columns = columns;
        this.recordTable.setRecordColumns(columns);
    }

    /**
     * 初始化记录
     *
     * @param records 数据
     */
    private void initRecords(List<SQLiteRecord> records) {
        this.recordTable.setItem(records);
    }

    /**
     * 添加记录
     */
    @FXML
    private void addRecord() {
        SQLiteRecord record = new SQLiteRecord(new SQLiteColumns(this.columns));
        record.setCreated(true);
        for (SQLiteColumn column : record.getColumns()) {
            Object val = null;
            if (column.supportDefaultValue()) {
                val = column.getDefaultValue();
            }
            record.putValue(column, val);
        }
        this.recordTable.addItem(record);
        this.recordTable.clearSelection();
        this.recordTable.selectLast();
        this.initCount(this.pageData.count() + 1);
    }

    /**
     * 插入记录
     *
     * @param record 记录
     */
    private void insertRecord(SQLiteRecord record) {
        DBRecordData recordData = record.getRecordData();
        SQLiteRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        if (primaryKey != null) {
            this.getItem().insertRecord(recordData, primaryKey);
            SQLiteRecord newRecord = this.getItem().selectRecord(primaryKey);
            if (newRecord != null) {
                record.copy(newRecord);
            }
        } else {
            this.getItem().insertRecord(recordData);
        }
    }

    /**
     * 更改记录
     *
     * @param record 记录
     */
    private void updateRecord(SQLiteRecord record) {
        SQLiteRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        if (primaryKey != null) {
            DBRecordData recordData = record.getChangedRecordData();
            if (!record.isColumnChanged(primaryKey.getColumnName())) {
                recordData.remove(primaryKey.getColumnName());
            }
            this.getItem().updateRecord(recordData, primaryKey);
            SQLiteRecord newRecord = this.getItem().selectRecord(primaryKey);
            if (newRecord != null) {
                record.copy(newRecord);
            }
        } else {
            DBRecordData changedRecordData = record.getChangedRecordData();
            DBRecordData originalRecordData = record.getOriginalRecordData();
            this.getItem().updateRecord(changedRecordData, originalRecordData);
        }
    }

    /**
     * 初始化主键
     *
     * @param record 记录
     * @return 主键
     */
    private SQLiteRecordPrimaryKey initPrimaryKey(SQLiteRecord record) {
        SQLiteColumn primaryKeyColumn = this.getItem().getPrimaryKey();
        if (primaryKeyColumn != null) {
            SQLiteRecordPrimaryKey primaryKey = new SQLiteRecordPrimaryKey();
            primaryKey.init(primaryKeyColumn, record);
            return primaryKey;
        }
        return null;
    }

    /**
     * 应用变更
     */
    @FXML
    private void apply() {
        if (this.apply.isEnable()) {
            try {
                List<SQLiteRecord> records = this.recordTable.getItems();
                for (SQLiteRecord record : records) {
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
                MessageBox.exception(ex, this.getWindow());
            }
        }
    }

    /**
     * 丢弃变更
     */
    @FXML
    private void discard() {
        try {
            SQLiteRecord discardRecord = null;
            for (SQLiteRecord record : this.recordTable.getItems()) {
                if (record.isCreated()) {
                    discardRecord = record;
                } else if (record.isChanged()) {
                    record.discard();
                }
            }
            this.recordTable.removeItem(discardRecord);
            this.apply.disable();
            this.initCount(this.pageData.count() - 1);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 刷新记录
     */
    @FXML
    public void reload() {
        StageManager.showMask(this.getWindow(), this::doReload);
    }

    /**
     * 刷新记录，实际业务
     */
    private void doReload() {
        try {
            if (this.apply.isEnable() && !MessageBox.confirm(I18nHelper.unsavedAndContinue(), this.getWindow())) {
                return;
            }
            this.initColumns(this.getItem().columns());
            this.initDataList(0);
//            this.missPrimaryKey.setVisible(!this.columns.hasPrimaryKey());
            this.filter.setActive(CollectionUtil.isNotEmpty(this.enabledFilters()));
            this.apply.disable();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 过滤记录
     */
    @FXML
    private void filter() {
        try {
            PopupAdapter popup = PopupManager.parsePopup(ShellSqliteRecordFilterPopupController.class);
            popup.setProp("item", this.getItem());
            popup.setProp("filters", this.filters);
            popup.showPopup(this.filter);
            popup.setSubmitHandler(filters -> {
                this.setFilters((List<SQLiteRecordFilter>) filters);
                this.reload();
            });
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
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
     *
     * @param event 分页跳转事件
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
        PopupAdapter popup = PopupManager.parsePopup(ShellDBPageSettingPopupController.class);
        popup.showPopup(this.pageBox.getSettingBtn());
        int limit = this.setting.getRecordPageLimit();
        popup.setSubmitHandler(o -> {
            if (o instanceof Integer value && value != limit) {
                this.firstPage();
            }
        });
    }

    /**
     * 删除记录
     */
    @FXML
    private void deleteRecord() {
        List<SQLiteRecord> records = new ArrayList<>(this.recordTable.getSelectedItems());
        if (!MessageBox.confirm(ShellI18nHelper.confirm("deleteRecord"), this.getWindow())) {
            return;
        }
        StageManager.showMask(this.getWindow(), () -> this.deleteRecords(records));
    }

    /**
     * 删除记录
     *
     * @param records 记录
     */
    private void deleteRecords(List<SQLiteRecord> records) {
        try {
            boolean success = false;
            for (SQLiteRecord record : records) {
                success = this.deleteRecord(record);
                if (!success) {
                    break;
                }
            }
            if (success) {
                this.recordTable.removeItem(records);
                this.initCount(this.pageData.count() - records.size());
            } else {
                MessageBox.warnToast(I18nHelper.operationFail(), this.getWindow());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 删除记录
     *
     * @param record 记录
     * @return 结果
     */
    private boolean deleteRecord(SQLiteRecord record) {
        boolean success;
        if (record.isCreated()) {
            success = true;
        } else {
            SQLiteRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
            if (primaryKey != null) {
                success = this.getItem().deleteRecord(primaryKey) == 1;
            } else {
                DBRecordData recordData = record.getOriginalRecordData();
                success = this.getItem().deleteRecord(recordData) == 1;
            }
            if (success) {
                record.destroy();
            }
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
        this.missPrimaryKey.disableTheme();
        this.discard.disableProperty().bind(this.apply.disableProperty());
        this.apply.disabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                NodeGroupUtil.enable(this.root, "action2");
            } else {
                NodeGroupUtil.disable(this.root, "action2");
            }
        });
        this.recordTable.getItems().addListener((ListChangeListener<SQLiteRecord>) c -> {
            if (c.next() && c.wasAdded()) {
                List<? extends SQLiteRecord> rows = c.getAddedSubList();
                for (SQLiteRecord row : rows) {
                    if (DBObjectList.isCreated(row)) {
                        this.apply.enable();
                        break;
                    }
                }
            }
        });
        this.recordTable.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.setEditable(false);
            }
            this.recordTable.refresh();
        });
        this.recordTable.setCtrlSAction(this::apply);
        NodeUtil.nodeOnCtrlS(this.root, this::apply);
    }

    /**
     * 获取过滤条件
     *
     * @return 过滤条件
     */
    public List<SQLiteRecordFilter> getFilters() {
        return filters;
    }

    /**
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<SQLiteRecordFilter> filters) {
        this.filters = filters;
    }
}
