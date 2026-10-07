package cn.oyzh.easyshell.fx.dameng;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * 达梦数据库模式选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengSchemaComboBox extends FXComboBox<String> {

    /**
     * 初始化模式列表
     *
     * @param client 达梦客户端
     */
    public void init(ShellDamengClient client) {
        this.init(client, null);
    }

    /**
     * 初始化模式列表并选中指定模式
     *
     * @param client 达梦客户端
     * @param schema 模式名称
     */
    public void init(ShellDamengClient client, String schema) {
        this.clearItems();
        List<DamengSchema> schemas = client.selectSchemas();
        if (CollectionUtil.isNotEmpty(schemas)) {
            this.setItem(schemas.stream().map(DamengSchema::getName).toList());
        }
        if (schema != null) {
            this.select(schema);
        }
    }
}
