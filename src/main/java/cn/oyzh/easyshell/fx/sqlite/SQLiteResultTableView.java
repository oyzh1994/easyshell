package cn.oyzh.easyshell.fx.sqlite;

import cn.oyzh.easyshell.fx.sqlite.record.ShellSqliteRecordColumn;
import cn.oyzh.easyshell.fx.sqlite.record.ShellSqliteRecordTableRow;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.fx.db.ui.DBStatusColumn;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import javafx.collections.FXCollections;
import javafx.scene.control.SelectionMode;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 查询结果表格。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteResultTableView extends FXTableView<SQLiteRecord> {

    /**
     * 当前字段列表
     */
    private List<SQLiteColumn> columns = List.of();

    /**
     * 设置查询结果。
     *
     * @param result 查询结果
     */
    public void setResult(SQLiteExecuteResult result) {
        this.setResult(result == null ? List.of() : result.getColumns(),
                result == null ? List.of() : result.getRecords());
    }

    /**
     * 设置字段和记录。
     *
     * @param columns 字段列表
     * @param records 记录列表
     */
    public void setResult(List<SQLiteColumn> columns, List<SQLiteRecord> records) {
        this.columns = columns == null ? List.of() : columns;
        List<FXTableColumn<SQLiteRecord, Object>> columnList = new ArrayList<>();
        columnList.add(new DBStatusColumn<>());
        for (SQLiteColumn column : this.columns) {
            ShellSqliteRecordColumn tableColumn = new ShellSqliteRecordColumn(column, false);
            tableColumn.setPrefWidth(DBUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        this.getColumns().setAll(columnList);
        this.setItems(FXCollections.observableArrayList(records == null ? List.of() : records));
    }

    public List<SQLiteColumn> getSqliteColumns() {
        return this.columns;
    }

    @Override
    public void initNode() {
        this.setSelectionMode(SelectionMode.MULTIPLE);
        this.setRowFactory(param -> new ShellSqliteRecordTableRow());
        super.destroyItemsOnRemoved();
        super.initNode();
    }
}
