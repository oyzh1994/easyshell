package cn.oyzh.easyshell.fx.sqlite;

import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.ShellSqliteHelper;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

import java.util.List;

/**
 * SQLite 查询结果表格。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteResultTableView extends FXTableView<DBRecordData> {

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
    public void setResult(List<SQLiteColumn> columns, List<DBRecordData> records) {
        this.columns = columns == null ? List.of() : columns;
        this.getColumns().clear();
        for (SQLiteColumn column : this.columns) {
            FXTableColumn<DBRecordData, String> tableColumn = new FXTableColumn<>(column.getName());
            tableColumn.setPrefWidth(DBUtil.suitableColumnWidth(column));
            tableColumn.setCellValueFactory(param ->
                    new SimpleObjectProperty<>(ShellSqliteHelper.displayValue(param.getValue().get(column))));
            this.getColumns().add(tableColumn);
        }
        this.setItems(FXCollections.observableArrayList(records == null ? List.of() : records));
    }

    public List<SQLiteColumn> getSqliteColumns() {
        return this.columns;
    }
}
