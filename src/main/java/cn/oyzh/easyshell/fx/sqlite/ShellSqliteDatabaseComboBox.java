package cn.oyzh.easyshell.fx.sqlite;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * SQLite 数据库下拉框。
 *
 * @author oyzh
 * @since 2026-11-04
 */
public class ShellSqliteDatabaseComboBox extends FXComboBox<String> {

    /**
     * 初始化数据库列表。
     *
     * @param client SQLite 客户端
     */
    public void init(ShellSqliteClient client) {
        this.init(client, null);
    }

    /**
     * 初始化数据库列表并选中指定数据库。
     *
     * @param client SQLite 客户端
     * @param dbName 数据库名称
     */
    public void init(ShellSqliteClient client, String dbName) {
        this.clearItems();
        List<String> databases = client.databaseNames();
        if (CollectionUtil.isNotEmpty(databases)) {
            this.setItem(databases);
        }
        if (StringUtil.isNotBlank(dbName)) {
            this.select(dbName);
        }
    }
}
