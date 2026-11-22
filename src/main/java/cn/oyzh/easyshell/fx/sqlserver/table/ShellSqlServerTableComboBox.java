package cn.oyzh.easyshell.fx.sqlserver.table;

import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * SqlServer数据表下拉选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerTableComboBox extends FXComboBox<String> {

    /**
     * 初始化数据表下拉选项
     *
     * @param schema 模式名
     * @param client SqlServer客户端
     */
    public void init(String schema, ShellSqlServerClient client) {
        this.init(schema, null, client);
    }

    /**
     * 初始化数据表下拉选项并选中指定数据表
     *
     * @param schema    模式名
     * @param tableName 数据表名
     * @param client    SqlServer客户端
     */
    public void init(String schema, String tableName, ShellSqlServerClient client) {
        List<SqlServerTable> list = client.selectTables(schema);
        this.setItem(list.parallelStream().map(SqlServerTable::getName).toList());
        if (tableName != null) {
            this.select(tableName);
        }
    }
}
