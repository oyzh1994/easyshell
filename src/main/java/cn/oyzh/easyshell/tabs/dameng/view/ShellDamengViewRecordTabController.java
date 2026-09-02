package cn.oyzh.easyshell.tabs.dameng.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengRecordData;
import cn.oyzh.easyshell.dameng.record.DamengRecordFilter;
import cn.oyzh.easyshell.dameng.record.DamengRecordPrimaryKey;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.dameng.record.DamengRecordColumn;
import cn.oyzh.easyshell.fx.dameng.record.DamengRecordTableView;
import cn.oyzh.easyshell.popups.dameng.DamengPageSettingPopupController;
import cn.oyzh.easyshell.popups.dameng.DamengViewRecordFilterPopupController;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewTreeItem;
import cn.oyzh.fx.db.DBObjectList;
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
public class ShellDamengViewRecordTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * db树视图节点
     */
    private ObjectProperty<ShellDamengViewTreeItem> itemProperty;

    /**
     * 分页数据
     */
    private Paging<DamengRecord> pageData;

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
    private PageBox<DamengRecord> pageBox;

    /**
     * 数据表单组件
     */
    @FXML
    private DamengRecordTableView recordTable;

    /**
     * 过滤列表
     */
    private List<DamengRecordFilter> filters;

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
    private DamengColumns columns;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 执行初始化
     *
     * @param item db树视图节点
     */
    public void init(ShellDamengViewTreeItem item) {
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
            // this.add.display();
            // this.apply.display();
            // this.delete.display();
            // this.discard.display();
        }
    }

    public ShellDamengViewTreeItem getItem() {
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
    private List<DamengRecordFilter> enabledFilters() {
        if (CollectionUtil.isNotEmpty(this.filters)) {
            return this.filters.stream().filter(DamengRecordFilter::isEnabled).toList();
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
    private void initColumns(DamengColumns columns) {
        // 设置字段列表
        this.columns = columns;
        // 数据列集合
        List<FXTableColumn<DamengRecord, Object>> columnList = new ArrayList<>();
        DBStatusColumn<DamengRecord> statusColumn = new DBStatusColumn<>();
        columnList.add(statusColumn);
        for (DamengColumn column : columns) {
            DamengRecordColumn tableColumn = new DamengRecordColumn(column);
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
    private void initRecords(List<DamengRecord> records) {
        this.recordTable.setItem(records);
    }

    /**
     * 添加记录
     */
    @FXML
    private void addRecord() {
        DamengRecord record = new DamengRecord(new DamengColumns(this.columns));
        record.setCreated(true);
        for (DamengColumn column : record.getColumns()) {
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
    private void insertRecord(DamengRecord record) {
        DamengRecordData recordData = record.getRecordData();
        DamengRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
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
    private void updateRecord(DamengRecord record) {
        // 获取主键
        DamengRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        // 主键存在，则根据主键更新
        if (primaryKey != null) {
            // 记录数据
            DamengRecordData recordData = record.getChangedRecordData();
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
            DamengRecordData changedRecordData = record.getChangedRecordData();
            // 原始数据
            DamengRecordData originalRecordData = record.getOriginalRecordData();
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
    private DamengRecordPrimaryKey initPrimaryKey(DamengRecord record) {
        DamengColumn primaryKeyColumn = this.getItem().getPrimaryKey();
        if (primaryKeyColumn != null) {
            DamengRecordPrimaryKey primaryKey = new DamengRecordPrimaryKey();
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
                List<DamengRecord> records = this.recordTable.getItems();
                for (DamengRecord record : records) {
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
            DamengRecord discardRecord = null;
            for (DamengRecord record : this.recordTable.getItems()) {
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
            PopupAdapter popup = PopupManager.parsePopup(DamengViewRecordFilterPopupController.class);
            popup.setProp("item", this.getItem());
            popup.setProp("filters", this.filters);
            popup.showPopup(this.filter);
            popup.setSubmitHandler(filters -> {
                this.setFilters((List<DamengRecordFilter>) filters);
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
        PopupAdapter popup = PopupManager.parsePopup(DamengPageSettingPopupController.class);
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
        // DamengRecord record = this.recordTable.getSelectedItem();
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
        //         DamengRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        //         // 主键存在，则根据主键删除
        //         if (primaryKey != null) {
        //             success = this.getItem().deleteRecord(primaryKey) == 1;
        //         } else {// 主键不存在，则根据所有字段更新
        //             // 所有字段数据
        //             DamengRecordData recordData = record.getOriginalRecordData();
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
        List<DamengRecord> records = new ArrayList<>(this.recordTable.getSelectedItems());
        if (!MessageBox.confirm(I18nHelper.deleteRecord() + "?")) {
            return;
        }
        StageManager.showMask(() -> this.deleteRecords(records));
    }

    /**
     * 删除记录
     *
     * @param records 记录
     */
    private void deleteRecords(List<DamengRecord> records) {
        try {
            boolean success = false;
            for (DamengRecord record : records) {
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
    private boolean deleteRecord(DamengRecord record) {
        // 如果是新增的数据，直接删除
        boolean success;
        if (record.isCreated()) {
            success = true;
        } else {
            // 获取主键
            DamengRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
            // 主键存在，则根据主键删除
            if (primaryKey != null) {
                success = this.getItem().deleteRecord(primaryKey) == 1;
            } else {// 主键不存在，则根据所有字段更新
                // 所有字段数据
                DamengRecordData recordData = record.getOriginalRecordData();
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
//        this.recordTable.destroy();
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
        this.recordTable.getItems().addListener((ListChangeListener<DamengRecord>) c -> {
            if (c.next() && c.wasAdded()) {
                List<? extends DamengRecord> rows = c.getAddedSubList();
                for (DamengRecord row : rows) {
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
    //         this.recordTable.getItems().addListener((ListChangeListener<DamengRecord>) c -> {
    //             if (c.next() && c.wasAdded()) {
    //                 List<? extends DamengRecord> rows = c.getAddedSubList();
    //                 for (DamengRecord row : rows) {
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

    public List<DamengRecordFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<DamengRecordFilter> filters) {
        this.filters = filters;
    }
}
