package cn.oyzh.easyshell.fx.postgresql;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.schema.PostgresqlSchema;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * PostgreSQL数据库模式选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlSchemaComboBox extends FXComboBox<String> {

    /**
     * 初始化模式列表
     *
     * @param client PostgreSQL客户端
     */
    public void init(ShellPostgresqlClient client) {
        this.init(client, null);
    }

    /**
     * 初始化模式列表并选中指定模式
     *
     * @param client PostgreSQL客户端
     * @param schema 模式名称
     */
    public void init(ShellPostgresqlClient client, String schema) {
        this.clearItems();
        List<PostgresqlSchema> schemas = client.selectSchemas();
        if (CollectionUtil.isNotEmpty(schemas)) {
            this.setItem(schemas.stream().map(PostgresqlSchema::getName).toList());
        }
        if (schema != null) {
            this.select(schema);
        }
    }
}
