package cn.oyzh.easyshell.fx.sqlite.table;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 数据表下拉选择框。
 *
 * @since 2026-11-03
 */
public class ShellSqliteTableComboBox extends FXComboBox<String> {

    /**
     * 初始化数据表下拉选项。
     *
     * @param tableName 数据表名称
     * @param client    SQLite客户端
     */
    public void init(String tableName, ShellSqliteClient client) {
        List<String> tables = client == null ? List.of()
                : client.selectTables().parallelStream().map(SQLiteTable::getName).toList();
        List<String> items = tables;
        if (tableName != null && tables.stream().noneMatch(item -> item.equalsIgnoreCase(tableName))) {
            items = new ArrayList<>(tables);
            items.add(tableName);
        }
        this.setItem(items);
        if (tableName != null) {
            this.select(tableName);
        } else {
            this.clearChild();
        }
    }
}
