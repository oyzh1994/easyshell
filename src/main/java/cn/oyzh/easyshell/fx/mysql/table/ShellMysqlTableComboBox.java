package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * 数据表选择框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlTableComboBox extends FXComboBox<String> {

    /**
     * 初始化数据表选择框
     *
     * @param dbName 数据库名称
     * @param client mysql客户端
     */
    public void init(String dbName, ShellMysqlClient client) {
        this.init(dbName, null, client);
    }

    /**
     * 初始化数据表选择框
     *
     * @param dbName    数据库名称
     * @param tableName 数据表名称
     * @param client    mysql客户端
     */
    public void init(String dbName, String tableName, ShellMysqlClient client) {
        List<MysqlTable> list = client.selectTables(dbName);
        this.setItem(list.parallelStream().map(MysqlTable::getName).toList());
        if (tableName != null) {
            this.select(tableName);
        } else {
            this.clearChild();
        }
    }
}
