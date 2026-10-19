package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * mongodb数据库选择框
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoDatabaseComboBox extends FXComboBox<String> {

    /**
     * 初始化数据库选项
     *
     * @param client 客户端
     */
    public void init(ShellMongoClient client) {
        this.init(client, null);
    }

    /**
     * 初始化数据库选项并选中指定数据库
     *
     * @param client 客户端
     * @param dbName 数据库名称
     */
    public void init(ShellMongoClient client, String dbName) {
        this.clearItems();
        List<String> databases = client.listDatabaseNames();
        if (CollectionUtil.isNotEmpty(databases)) {
            this.setItem(databases);
        }
        if (StringUtil.isNotBlank(dbName)) {
            this.select(dbName);
        }
    }
}
