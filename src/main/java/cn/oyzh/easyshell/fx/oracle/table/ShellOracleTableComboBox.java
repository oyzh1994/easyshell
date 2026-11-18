package cn.oyzh.easyshell.fx.oracle.table;

import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * Oracle数据表下拉选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleTableComboBox extends FXComboBox<String> {

    /**
     * 初始化数据表下拉选项
     *
     * @param schema 模式名
     * @param client Oracle客户端
     */
    public void init(String schema, ShellOracleClient client) {
        this.init(schema, null, client);
    }

    /**
     * 初始化数据表下拉选项并选中指定数据表
     *
     * @param schema    模式名
     * @param tableName 数据表名
     * @param client    Oracle客户端
     */
    public void init(String schema, String tableName, ShellOracleClient client) {
        List<OracleTable> list = client.selectTables(schema);
        this.setItem(list.parallelStream().map(OracleTable::getName).toList());
        if (tableName != null) {
            this.select(tableName);
        }
    }
}
