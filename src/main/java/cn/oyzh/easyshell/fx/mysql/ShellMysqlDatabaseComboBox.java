package cn.oyzh.easyshell.fx.mysql;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dto.mysql.MysqlDatabase;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * db数据库选择框
 *
 * @author oyzh
 * @since 2024/01/25
 */
public class ShellMysqlDatabaseComboBox extends FXComboBox<String> {

    public void init(ShellMysqlClient client) {
        this.init(client, null);
    }

    public void init(ShellMysqlClient client, String dbName) {
        this.clearItems();
        List<MysqlDatabase> databases = client.databases();
        if (CollectionUtil.isNotEmpty(databases)) {
            this.setItem(databases.stream().map(MysqlDatabase::getName).toList());
        }
        if (dbName != null) {
            this.select(dbName);
        }
    }
}
