package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * db数据库选择框
 *
 * @author oyzh
 * @since 2024/01/25
 */
public class MysqlTableComboBox extends FXComboBox<String> {

    public void init(String dbName, ShellMysqlClient client) {
        this.init(dbName, null, client);
    }

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
