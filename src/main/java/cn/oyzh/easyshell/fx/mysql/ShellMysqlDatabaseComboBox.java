package cn.oyzh.easyshell.fx.mysql;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.database.MysqlDatabase;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * MySQL数据库下拉框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlDatabaseComboBox extends FXComboBox<String> {

    /**
     * 初始化数据库列表
     *
     * @param client MySQL客户端
     */
    public void init(ShellMysqlClient client) {
        this.init(client, null);
    }

    /**
     * 初始化数据库列表并选中指定数据库
     *
     * @param client MySQL客户端
     * @param dbName 数据库名称
     */
    public void init(ShellMysqlClient client, String dbName) {
        this.clearItems();
        List<MysqlDatabase> databases = client.databases();
        if (CollectionUtil.isNotEmpty(databases)) {
            this.setItem(databases.stream().map(MysqlDatabase::getName).toList());
        }
        if (StringUtil.isNotBlank(dbName)) {
            this.select(dbName);
        }
    }
}
