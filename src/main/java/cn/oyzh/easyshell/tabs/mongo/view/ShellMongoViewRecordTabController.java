package cn.oyzh.easyshell.tabs.mongo.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.CollectionUtil;
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
import cn.oyzh.easyshell.trees.mongo.view.ShellMongoViewTreeItem;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewFactory;
import cn.oyzh.fx.db.ui.DBStatusColumn;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.page.PageBox;
import cn.oyzh.fx.gui.page.PageEvent;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MongoDB 视图记录标签页业务。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoViewRecordTabController extends RichTabController {

    @FXML
    private FXVBox root;

    @FXML
    private SVGGlyph filter;

    @FXML
    private PageBox<MongoRecord> pageBox;

    @FXML
    private ShellMongoRecordTableView recordTable;

    private ObjectProperty<ShellMongoViewTreeItem> itemProperty;

    private Paging<MongoRecord> pageData;

    private List<MongoRecordFilter> filters;

    private MongoColumns columns;

    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 初始化
     *
     * @param item 视图节点
     */
    public void init(ShellMongoViewTreeItem item) {
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
        this.recordTable.setEditable(false);
        this.reload();
    }

    public ShellMongoViewTreeItem getItem() {
        return this.itemProperty == null ? null : this.itemProperty.get();
    }

    private void initDataList(long pageNo) {
        try {
            this.pageData = this.getItem().recordPage(pageNo,
                    this.setting.getMongoRecordPageLimit(), this.enabledFilters(), this.columns);
            this.pageBox.setPaging(this.pageData);
            List<MongoRecord> records = this.pageData.dataList();
            this.updateColumns(records);
            this.initRecords(records);
            this.correctRecords();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    private void initDataListByMask(long pageNo) {
        StageManager.showMask(this.getWindow(), () -> this.initDataList(pageNo));
    }

    private List<MongoRecordFilter> enabledFilters() {
        if (CollectionUtil.isNotEmpty(this.filters)) {
            return this.filters.stream().filter(MongoRecordFilter::isEnabled).toList();
        }
        return null;
    }

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

    private void initColumns(MongoColumns columns) {
        this.columns = columns;
        if (this.columns == null) {
            this.recordTable.clearColumn();
            return;
        }
        List<FXTableColumn<MongoRecord, Object>> columnList = new ArrayList<>();
        columnList.add(new DBStatusColumn<>());
        for (MongoColumn column : columns) {
            ShellMongoRecordColumn tableColumn = new ShellMongoRecordColumn(column);
            tableColumn.setPrefWidth(DBUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        this.recordTable.setColumn(columnList);
    }

    private void initRecords(List<MongoRecord> records) {
        for (MongoRecord record : records) {
            record.setEditable(false);
        }
        this.recordTable.setItem(records);
    }

    private void correctRecords() {
        for (MongoRecord record : this.recordTable.getItems()) {
            record.correctColumns(this.columns);
        }
    }

    @FXML
    public void reload() {
        StageManager.showMask(this.getWindow(), this::doReload);
    }

    private void doReload() {
        try {
            this.initDataListByMask(0);
            this.filter.setActive(CollectionUtil.isNotEmpty(this.enabledFilters()));
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @FXML
    private void filter() {
        try {
            PopupAdapter popup = PopupManager.parsePopup(ShellMongoRecordFilterPopupController.class);
            popup.setProp("filters", this.filters);
            popup.setProp("columns", this.columns);
            popup.showPopup(this.filter);
            popup.setSubmitHandler(filterList -> {
                this.filters = (List<MongoRecordFilter>) filterList;
                this.reload();
            });
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @FXML
    private void nextPage() {
        this.initDataListByMask(this.pageData.nextPage());
    }

    @FXML
    private void prevPage() {
        this.initDataListByMask(this.pageData.prevPage());
    }

    @FXML
    private void lastPage() {
        this.initDataListByMask(this.pageData.lastPage());
    }

    @FXML
    private void firstPage() {
        this.initDataListByMask(0);
    }

    @FXML
    private void pageJump(PageEvent.PageJumpEvent event) {
        this.initDataListByMask(event.getPage());
    }

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

    @FXML
    private void exportData() {
        ShellMongoViewFactory.exportData(this.getItem().client(),
                this.getItem().dbName(), this.getItem().viewName());
    }

    public void setFilters(List<MongoRecordFilter> filters) {
        this.filters = filters;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.recordTable.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.setEditable(false);
            }
            this.recordTable.refresh();
        });
    }
}
