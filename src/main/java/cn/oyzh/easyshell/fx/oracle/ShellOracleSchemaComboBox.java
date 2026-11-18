package cn.oyzh.easyshell.fx.oracle;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * Oracle数据库模式选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleSchemaComboBox extends FXComboBox<String> {

    /**
     * 初始化模式列表
     *
     * @param client Oracle客户端
     */
    public void init(ShellOracleClient client) {
        this.init(client, null);
    }

    /**
     * 初始化模式列表并选中指定模式
     *
     * @param client Oracle客户端
     * @param schema 模式名称
     */
    public void init(ShellOracleClient client, String schema) {
        this.clearItems();
        List<OracleSchema> schemas = client.selectSchemas();
        if (CollectionUtil.isNotEmpty(schemas)) {
            this.setItem(schemas.stream().map(OracleSchema::getName).toList());
        }
        if (schema != null) {
            this.select(schema);
        }
    }
}
