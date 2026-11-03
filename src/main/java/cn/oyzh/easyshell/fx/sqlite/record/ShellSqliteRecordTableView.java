package cn.oyzh.easyshell.fx.sqlite.record;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordProperty;
import cn.oyzh.fx.db.ui.DBStatusColumn;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.scene.control.SelectionMode;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite记录表格视图。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteRecordTableView extends FXTableView<SQLiteRecord> {

    /**
     * 设置记录列
     *
     * @param columns 字段列表
     */
    public void setRecordColumns(List<SQLiteColumn> columns) {
        List<FXTableColumn<SQLiteRecord, Object>> columnList = new ArrayList<>();
        columnList.add(new DBStatusColumn<>());
        for (SQLiteColumn column : columns) {
            ShellSqliteRecordColumn tableColumn = new ShellSqliteRecordColumn(column);
            tableColumn.setPrefWidth(DBUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        this.setColumn(columnList);
    }

    /**
     * 是否存在记录属性
     *
     * @param recordProperty 记录属性
     * @return 结果
     */
    public boolean hasProperty(SQLiteRecordProperty recordProperty) {
        if (recordProperty != null) {
            for (SQLiteRecord record : this.getItems()) {
                if (record.hasProperty(recordProperty)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 是否存在记录
     *
     * @param record 记录
     * @return 结果
     */
    public boolean hasRecord(SQLiteRecord record) {
        return record != null && this.getItems().contains(record);
    }

    @Override
    public void initNode() {
        this.setSelectionMode(SelectionMode.MULTIPLE);
        this.setRowFactory(param -> new ShellSqliteRecordTableRow());
        super.destroyItemsOnRemoved();
        super.initNode();
    }
}
