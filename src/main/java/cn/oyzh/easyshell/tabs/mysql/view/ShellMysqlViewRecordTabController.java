package cn.oyzh.easyshell.tabs.mysql.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.db.DBStatusColumn;
import cn.oyzh.easyshell.fx.mysql.record.ShellMysqlRecordColumn;
import cn.oyzh.easyshell.fx.mysql.record.ShellMysqlRecordTableView;
import cn.oyzh.easyshell.db.DBObjectList;
import cn.oyzh.easyshell.db.listener.DBStatusListener;
import cn.oyzh.easyshell.db.listener.DBStatusListenerManager;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlRecordData;
import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
import cn.oyzh.easyshell.mysql.record.MysqlRecordPrimaryKey;
import cn.oyzh.easyshell.popups.mysql.ShellMysqlPageSettingPopupController;
import cn.oyzh.easyshell.popups.mysql.ShellMysqlTableRecordFilterPopupController;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewTreeItem;
import cn.oyzh.easyshell.util.mysql.ShellMysqlRecordUtil;
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
 * @since 2024/06/28
 */
public class ShellMysqlViewRecordTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * db树视图节点
     */
    private ObjectProperty<ShellMysqlViewTreeItem> itemProperty;

    /**
     * 分页数据
     */
    private Paging<MysqlRecord> pageData;

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
    private PageBox<MysqlRecord> pageBox;

    /**
     * 数据表单组件
     */
    @FXML
    private ShellMysqlRecordTableView recordTable;

    /**
     * 过滤列表
     */
    private List<MysqlRecordFilter> filters;

    // /**
    //  * 新增
    //  */
    // @FXML
    // private SVGGlyph add;
    //
    // /**
    //  * 删除
    //  */
    // @FXML
    // private SVGGlyph delete;

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
    private MysqlColumns columns;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 执行初始化
     *
     * @param item db树视图节点
     */
    public void init(ShellMysqlViewTreeItem item) {
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
            // this.add.display();
            // this.apply.display();
            // this.delete.display();
            // this.discard.display();
        }
    }

    public ShellMysqlViewTreeItem getItem() {
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
            MessageBox.exception(ex);
        }
    }

    /**
     * 获取已启用的表过滤条件
     *
     * @return 已启用的表过滤条件
     */
    private List<MysqlRecordFilter> enabledFilters() {
        if (CollectionUtil.isNotEmpty(this.filters)) {
            return this.filters.stream().filter(MysqlRecordFilter::isEnabled).toList();
        }
        return null;
    }

    /**
     * 初始化列
     *
     * @param columns 列数据
     */
    private void initColumns(MysqlColumns columns) {
        // 设置字段列表
        this.columns = columns;
        // 数据列集合
        List<FXTableColumn<MysqlRecord, Object>> columnList = new ArrayList<>();
        DBStatusColumn<MysqlRecord> statusColumn = new DBStatusColumn<>();
        columnList.add(statusColumn);
        for (MysqlColumn column : columns) {
            ShellMysqlRecordColumn tableColumn = new ShellMysqlRecordColumn(column);
            tableColumn.setPrefWidth(ShellMysqlRecordUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        // FXUtil.runWait(() -> this.recordTable.getColumns().setAll(columnList));
        this.recordTable.setColumnsAll(columnList);
    }

    /**
     * 初始化记录
     *
     * @param records 数据
     */
    private void initRecords(List<MysqlRecord> records) {
        this.recordTable.setItem(records);
    }

    /**
     * 添加记录
     */
    @FXML
    private void addRecord() {
        MysqlRecord record = new MysqlRecord(this.columns);
        record.setCreated(true);
        for (MysqlColumn column : this.columns) {
            Object val = null;
            if (column.supportDefaultValue()) {
                val = column.getDefaultValue();
            }
            record.putValue(column, val);
        }
        this.recordTable.addItem(record);
        this.recordTable.selectLast();
    }

    /**
     * 插入记录
     *
     * @param record 记录
     */
    private void insertRecord(MysqlRecord record) {
        MysqlRecordData recordData = record.getRecordData();
        MysqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
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
    private void updateRecord(MysqlRecord record) {
        // 获取主键
        MysqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        // 主键存在，则根据主键更新
        if (primaryKey != null) {
            // 记录数据
            MysqlRecordData recordData = record.getChangedRecordData();
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
            MysqlRecordData changedRecordData = record.getChangedRecordData();
            // 原始数据
            MysqlRecordData originalRecordData = record.getOriginalRecordData();
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
    private MysqlRecordPrimaryKey initPrimaryKey(MysqlRecord record) {
        MysqlColumn primaryKeyColumn = this.getItem().getPrimaryKey();
        if (primaryKeyColumn != null) {
            MysqlRecordPrimaryKey primaryKey = new MysqlRecordPrimaryKey();
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
                List<MysqlRecord> records = this.recordTable.getItems();
                for (MysqlRecord record : records) {
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
            MysqlRecord discardRecord = null;
            for (MysqlRecord record : this.recordTable.getItems()) {
                if (record.isCreated()) {
                    discardRecord = record;
                } else if (record.isChanged()) {
                    record.discard();
                }
            }
            this.recordTable.removeItem(discardRecord);
            this.apply.disable();
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
            // 初始化字段
            this.initColumns(this.getItem().columns());
            // 初始化数据
            this.initDataList(0);
            // 判断是否缺少主键列
            this.missPrimaryKey.setVisible(!this.columns.hasPrimaryKey());
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
            PopupAdapter popup = PopupManager.parsePopup(ShellMysqlTableRecordFilterPopupController.class);
            popup.setProp("item", this.getItem());
            popup.setProp("filters", this.filters);
            popup.showPopup(this.filter);
            popup.setSubmitHandler(filters -> {
                this.setFilters((List<MysqlRecordFilter>) filters);
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
        this.initDataList(this.pageData.nextPage());
    }

    /**
     * 上一页
     */
    @FXML
    private void prevPage() {
        this.initDataList(this.pageData.prevPage());
    }

    /**
     * 尾页
     */
    @FXML
    private void lastPage() {
        this.initDataList(this.pageData.lastPage());
    }

    /**
     * 首页
     */
    @FXML
    private void firstPage() {
        this.initDataList(0);
    }

    /**
     * 跳页
     */
    @FXML
    private void pageJump(PageEvent.PageJumpEvent event) {
        this.initDataList(event.getPage());
    }

    /**
     * 页码设置
     */
    @FXML
    private void pageSetting() {
        PopupAdapter popup = PopupManager.parsePopup(ShellMysqlPageSettingPopupController.class);
        popup.showPopup(this.pageBox.getSettingBtn());
        int limit = this.setting.getRecordPageLimit();
        popup.setSubmitHandler(o -> {
            if (o instanceof Integer l && l != limit) {
                this.firstPage();
            }
        });
    }

    // /**
    //  * 删除记录
    //  */
    // @EventSubscribe
    // private void deleteRecord(RecordDeleteEvent event) {
    //     if (this.recordTable.hasRecord(event.data())) {
    //         this.doDeleteRecord(event.data());
    //     }
    // }

    /**
     * 删除记录
     */
    @FXML
    private void deleteRecord() {
        // try {
        // MysqlRecord record = this.recordTable.getSelectedItem();
        //     if (record == null) {
        //         return;
        //     }
        //     if (!MessageBox.confirm(I18nHelper.deleteRecord() + "?")) {
        //         return;
        //     }
        //     // 如果是新增的数据，直接删除
        //     boolean success;
        //     if (record.isCreated()) {
        //         success = true;
        //     } else {
        //         // 获取主键
        //         MysqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        //         // 主键存在，则根据主键删除
        //         if (primaryKey != null) {
        //             success = this.getItem().deleteRecord(primaryKey) == 1;
        //         } else {// 主键不存在，则根据所有字段更新
        //             // 所有字段数据
        //             MysqlRecordData recordData = record.getOriginalRecordData();
        //             // 删除行
        //             success = this.getItem().deleteRecord(recordData) == 1;
        //         }
        //     }
        //     // 操作成功
        //     if (success) {
        //         this.recordTable.removeItem(record);
        //     } else {// 操作失败
        //         MessageBox.warnToast(I18nHelper.operationFail());
        //     }
        // } catch (Exception ex) {
        //     MessageBox.exception(ex);
        // }
        List<MysqlRecord> records = this.recordTable.getSelectedItems();
        if (!MessageBox.confirm(I18nHelper.deleteRecord() + "?")) {
            return;
        }
        try {
            boolean success = false;
            for (MysqlRecord record : records) {
                success = this.doDeleteRecord(record);
                if (!success) {
                    break;
                }
            }
            // 操作成功
            if (success) {
                this.recordTable.removeItem(records);
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
    private boolean doDeleteRecord(MysqlRecord record) {
        // 如果是新增的数据，直接删除
        boolean success;
        if (record.isCreated()) {
            success = true;
        } else {
            // 获取主键
            MysqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
            // 主键存在，则根据主键删除
            if (primaryKey != null) {
                success = this.getItem().deleteRecord(primaryKey) == 1;
            } else {// 主键不存在，则根据所有字段更新
                // 所有字段数据
                MysqlRecordData recordData = record.getOriginalRecordData();
                // 删除行
                success = this.getItem().deleteRecord(recordData) == 1;
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
        this.recordTable.getItems().addListener((ListChangeListener<MysqlRecord>) c -> {
            if (c.next() && c.wasAdded()) {
                List<? extends MysqlRecord> rows = c.getAddedSubList();
                for (MysqlRecord row : rows) {
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

    // @Override
    // public void initialize(URL url, ResourceBundle resourceBundle) {
    //     try {
    //         super.initialize(url, resourceBundle);
    //         // this.add.managedBindVisible();
    //         // this.delete.managedBindVisible();
    //         this.missPrimaryKey.disableTheme();
    //         this.discard.disableProperty().bind(this.apply.disableProperty());
    //         this.apply.disabledProperty().addListener((observable, oldValue, newValue) -> {
    //             if (newValue) {
    //                 NodeGroupUtil.enable(this.root, "action2");
    //             } else {
    //                 NodeGroupUtil.disable(this.root, "action2");
    //             }
    //         });
    //         this.recordTable.getItems().addListener((ListChangeListener<MysqlRecord>) c -> {
    //             if (c.next() && c.wasAdded()) {
    //                 List<? extends MysqlRecord> rows = c.getAddedSubList();
    //                 for (MysqlRecord row : rows) {
    //                     if (DBObjectList.isCreated(row)) {
    //                         this.apply.enable();
    //                         break;
    //                     }
    //                 }
    //             }
    //         });
    //         this.recordTable.selectedItemChanged((observable, oldValue, newValue) -> {
    //             if (newValue != null) {
    //                 newValue.setEditable(true);
    //             }
    //             this.recordTable.refresh();
    //         });
    //         this.recordTable.setCtrlSAction(this::apply);
    //         NodeUtil.nodeOnCtrlS(this.root, this::apply);
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //     }
    // }

    public List<MysqlRecordFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<MysqlRecordFilter> filters) {
        this.filters = filters;
    }
}
