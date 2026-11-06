package cn.oyzh.easyshell.fx.postgresql.table;

import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * PostgreSQL数据表下拉选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlTableComboBox extends FXComboBox<String> {

    /**
     * 初始化数据表下拉选项
     *
     * @param schema 模式名
     * @param client PostgreSQL客户端
     */
    public void init(String schema, ShellPostgresqlClient client) {
        this.init(schema, null, client);
    }

    /**
     * 初始化数据表下拉选项并选中指定数据表
     *
     * @param schema    模式名
     * @param tableName 数据表名
     * @param client    PostgreSQL客户端
     */
    public void init(String schema, String tableName, ShellPostgresqlClient client) {
        List<PostgresqlTable> list = client.selectTables(schema);
        this.setItem(list.parallelStream().map(PostgresqlTable::getName).toList());
        if (tableName != null) {
            this.select(tableName);
        }
    }
}
