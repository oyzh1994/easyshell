package cn.oyzh.easyshell.data.sqlite.ui;

import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataExportTable;
import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 数据导出表选择表格。
 *
 * @since 2026-11-02
 */
public class SQLiteDataExportTableTableView extends FXTableView<SQLiteDataExportTable> {

    public List<SQLiteDataExportTable> getSelectedTables() {
        List<SQLiteDataExportTable> tables = new ArrayList<>();
        for (SQLiteDataExportTable table : this.getItems()) {
            if (table.isSelected()) {
                tables.add(table);
            }
        }
        return tables;
    }

    public boolean hasSelectedTable() {
        return !this.getSelectedTables().isEmpty();
    }
}
