package cn.oyzh.easyshell.fx.sqlserver;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.schema.SqlServerSchema;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * SqlServer数据库模式选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerSchemaComboBox extends FXComboBox<String> {

    /**
     * 初始化模式列表
     *
     * @param client SqlServer客户端
     */
    public void init(ShellSqlServerClient client) {
        this.init(client, null);
    }

    /**
     * 初始化模式列表并选中指定模式
     *
     * @param client SqlServer客户端
     * @param schema 模式名称
     */
    public void init(ShellSqlServerClient client, String schema) {
        this.clearItems();
        List<SqlServerSchema> schemas = client.selectSchemas();
        if (CollectionUtil.isNotEmpty(schemas)) {
            this.setItem(schemas.stream().map(SqlServerSchema::getName).toList());
        }
        if (schema != null) {
            this.select(schema);
        }
    }
}
