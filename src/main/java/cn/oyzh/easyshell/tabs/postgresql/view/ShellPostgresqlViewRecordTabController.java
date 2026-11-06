package cn.oyzh.easyshell.tabs.postgresql.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordFilter;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordPrimaryKey;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.postgresql.record.ShellPostgresqlRecordColumn;
import cn.oyzh.easyshell.fx.postgresql.record.ShellPostgresqlRecordTableView;
import cn.oyzh.easyshell.popups.postgresql.ShellPostgresqlRecordFilterPopupController;
import cn.oyzh.easyshell.popups.db.ShellDBPageSettingPopupController;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.trees.postgresql.view.ShellPostgresqlViewTreeItem;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.db.ui.DBStatusColumn;
import cn.oyzh.fx.db.util.DBUtil;
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
 * db视图tab内容组件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlViewRecordTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * db树视图节点
     */
    private ObjectProperty<ShellPostgresqlViewTreeItem> itemProperty;

    /**
     * 分页数据
     */
    private Paging<PostgresqlRecord> pageData;

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
    private PageBox<PostgresqlRecord> pageBox;

    /**
     * 数据表单组件
     */
    @FXML
    private ShellPostgresqlRecordTableView recordTable;

    /**
     * 过滤列表
     */
    private List<PostgresqlRecordFilter> filters;

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
    private PostgresqlColumns columns;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 执行初始化
     *
     * @param item db树视图节点
     */
    public void init(ShellPostgresqlViewTreeItem item) {
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
        // PostgreSQL视图判断是否可编辑不可靠，默认开放数据编辑
        if (item.isUpdatable()) {
            if (this.changeListener == null) {
                this.changeListener = new DBStatusListener(this.getItem().schema() + ":" + this.getItem().viewName()) {
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
    public ShellPostgresqlViewTreeItem getItem() {
        return this.itemProperty.get();
    }

    /**
     * 初始化数据列表
     *
     * @param pageNo 数据页码
     */
    private void initDataList(long pageNo) {
        try {
            this.pageData = this.getItem().recordPage(pageNo, this.setting.getRecordPageLimit(), this.enabledFilters(), this.columns);
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
    private List<PostgresqlRecordFilter> enabledFilters() {
        if (CollectionUtil.isNotEmpty(this.filters)) {
            return this.filters.stream().filter(PostgresqlRecordFilter::isEnabled).toList();
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
    private void initColumns(PostgresqlColumns columns) {
        // 设置字段列表
        this.columns = columns;
        // 数据列集合
        List<FXTableColumn<PostgresqlRecord, Object>> columnList = new ArrayList<>();
        DBStatusColumn<PostgresqlRecord> statusColumn = new DBStatusColumn<>();
        columnList.add(statusColumn);
        for (PostgresqlColumn column : columns) {
            ShellPostgresqlRecordColumn tableColumn = new ShellPostgresqlRecordColumn(column);
            tableColumn.setPrefWidth(DBUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        this.recordTable.setColumn(columnList);
    }

    /**
     * 初始化记录
     *
     * @param records 数据
     */
    private void initRecords(List<PostgresqlRecord> records) {
        this.recordTable.setItem(records);
    }

    /**
     * 添加记录
     */
    @FXML
    private void addRecord() {
        PostgresqlRecord record = new PostgresqlRecord(new PostgresqlColumns(this.columns));
        record.setCreated(true);
        for (PostgresqlColumn column : record.getColumns()) {
            Object val = null;
            if (column.supportDefaultValue()) {
                val = column.getDefaultValue();
            }
            record.putValue(column, val);
        }
        this.recordTable.addItem(record);
        this.recordTable.clearSelection();
        this.recordTable.selectLast();
        // 初始化计数
        this.initCount(this.pageData.count() + 1);
    }

    /**
     * 插入记录
     *
     * @param record 记录
     */
    private void insertRecord(PostgresqlRecord record) {
        DBRecordData recordData = record.getRecordData();
        PostgresqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        if (primaryKey != null) {
            this.getItem().insertRecord(recordData, primaryKey);
            // 处理回显
            record.copy(this.getItem().selectRecord(primaryKey));
        } else {
            this.getItem().insertRecord(recordData);
        }
    }

    /**
     * 更改记录
     *
     * @param record 记录
     */
    private void updateRecord(PostgresqlRecord record) {
        // 获取主键
        PostgresqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        // 主键存在，则根据主键更新
        if (primaryKey != null) {
            // 记录数据
            DBRecordData recordData = record.getChangedRecordData();
            // 如果主键未变更，则移除主键数据
            if (!record.isColumnChanged(primaryKey.getColumnName())) {
                recordData.remove(primaryKey.getColumnName());
            }
            // 更新行
            this.getItem().updateRecord(recordData, primaryKey);
            // 处理回显
            record.copy(this.getItem().selectRecord(primaryKey));
        } else {// 主键不存在，则根据所有字段更新
            // 变更数据
            DBRecordData changedRecordData = record.getChangedRecordData();
            // 原始数据
            DBRecordData originalRecordData = record.getOriginalRecordData();
            // 更新行
            this.getItem().updateRecord(changedRecordData, originalRecordData);
        }
    }

    /**
     * 初始化主键
     *
     * @param record 记录
     * @return 主键
     */
    private PostgresqlRecordPrimaryKey initPrimaryKey(PostgresqlRecord record) {
        // 主键
        PostgresqlColumn primaryKeyColumn = this.getItem().getPrimaryKey();
        if (primaryKeyColumn != null) {
            PostgresqlRecordPrimaryKey primaryKey = new PostgresqlRecordPrimaryKey();
            primaryKey.init(primaryKeyColumn, record);
            return primaryKey;
        }
        // 序列
        for (PostgresqlColumn column : record.getColumns()) {
            if (column.isDefaultValueSequence()) {
                PostgresqlRecordPrimaryKey primaryKey = new PostgresqlRecordPrimaryKey();
                primaryKey.init(column, record);
                return primaryKey;
            }
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
                List<PostgresqlRecord> records = this.recordTable.getItems();
                for (PostgresqlRecord record : records) {
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
            PostgresqlRecord discardRecord = null;
            for (PostgresqlRecord record : this.recordTable.getItems()) {
                if (record.isCreated()) {
                    discardRecord = record;
                } else if (record.isChanged()) {
                    record.discard();
                }
            }
            this.recordTable.removeItem(discardRecord);
            this.apply.disable();
            // 初始化计数
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
            // 检查是否有未保存的数据
            if (this.apply.isEnable() && !MessageBox.confirm(I18nHelper.unsavedAndContinue(), this.getWindow())) {
                return;
            }
            // 初始化字段
            this.initColumns(this.getItem().columns());
            // 初始化数据
            this.initDataList(0);
            // 判断是否缺少主键列
            this.missPrimaryKey.setVisible(!this.columns.hasPrimaryKey() && !this.columns.isDefaultValueSequence());
            // 设置过滤激活
            this.filter.setActive(CollectionUtil.isNotEmpty(this.enabledFilters()));
            // 禁用组件
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
            PopupAdapter popup = PopupManager.parsePopup(ShellPostgresqlRecordFilterPopupController.class);
            popup.setProp("item", this.getItem());
            popup.setProp("filters", this.filters);
            popup.showPopup(this.filter);
            popup.setSubmitHandler(filters -> {
                this.setFilters((List<PostgresqlRecordFilter>) filters);
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
     *
     * @param event 跳页事件
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
        List<PostgresqlRecord> records = new ArrayList<>(this.recordTable.getSelectedItems());
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
    private void deleteRecords(List<PostgresqlRecord> records) {
        try {
            boolean success = false;
            for (PostgresqlRecord record : records) {
                success = this.deleteRecord(record);
                if (!success) {
                    break;
                }
            }
            // 操作成功
            if (success) {
                this.recordTable.removeItem(records);
                // 初始化计数
                this.initCount(this.pageData.count() - records.size());
            } else {// 操作失败
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
    private boolean deleteRecord(PostgresqlRecord record) {
        // 如果是新增的数据，直接删除
        boolean success;
        if (record.isCreated()) {
            success = true;
        } else {
            // 获取主键
            PostgresqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
            // 主键存在，则根据主键删除
            if (primaryKey != null) {
                success = this.getItem().deleteRecord(primaryKey) == 1;
            } else {// 主键不存在，则根据所有字段更新
                // 所有字段数据
                DBRecordData recordData = record.getOriginalRecordData();
                // 删除行
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
        this.recordTable.getItems().addListener((ListChangeListener<PostgresqlRecord>) c -> {
            if (c.next() && c.wasAdded()) {
                List<? extends PostgresqlRecord> rows = c.getAddedSubList();
                for (PostgresqlRecord row : rows) {
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

    /**
     * 获取过滤条件
     *
     * @return 过滤条件
     */
    public List<PostgresqlRecordFilter> getFilters() {
        return filters;
    }

    /**
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<PostgresqlRecordFilter> filters) {
        this.filters = filters;
    }
}
