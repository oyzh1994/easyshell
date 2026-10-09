package cn.oyzh.easyshell.fx.mariadb.table;

import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * 数据表选择框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTableComboBox extends FXComboBox<String> {

    /**
     * 初始化数据表选择框
     *
     * @param dbName 数据库名称
     * @param client MariaDB客户端
     */
    public void init(String dbName, ShellMariadbClient client) {
        this.init(dbName, null, client);
    }

    /**
     * 初始化数据表选择框
     *
     * @param dbName    数据库名称
     * @param tableName 数据表名称
     * @param client    MariaDB客户端
     */
    public void init(String dbName, String tableName, ShellMariadbClient client) {
        List<MariadbTable> list = client.selectTables(dbName);
        this.setItem(list.parallelStream().map(MariadbTable::getName).toList());
        if (tableName != null) {
            this.select(tableName);
        } else {
            this.clearChild();
        }
    }
}
