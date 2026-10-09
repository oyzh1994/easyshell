package cn.oyzh.easyshell.fx.mariadb;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.database.MariadbDatabase;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * MariaDB数据库下拉框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbDatabaseComboBox extends FXComboBox<String> {

    /**
     * 初始化数据库列表
     *
     * @param client MariaDB客户端
     */
    public void init(ShellMariadbClient client) {
        this.init(client, null);
    }

    /**
     * 初始化数据库列表并选中指定数据库
     *
     * @param client MariaDB客户端
     * @param dbName 数据库名称
     */
    public void init(ShellMariadbClient client, String dbName) {
        this.clearItems();
        List<MariadbDatabase> databases = client.databases();
        if (CollectionUtil.isNotEmpty(databases)) {
            this.setItem(databases.stream().map(MariadbDatabase::getName).toList());
        }
        if (StringUtil.isNotBlank(dbName)) {
            this.select(dbName);
        }
    }
}
