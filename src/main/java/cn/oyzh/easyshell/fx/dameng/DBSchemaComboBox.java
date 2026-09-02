package cn.oyzh.easyshell.fx.dameng;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * db数据库选择框
 *
 * @author oyzh
 * @since 2024/01/25
 */
public class DBSchemaComboBox extends FXComboBox<String> {

    public void init(ShellDamengClient client) {
        this.init(client, null);
    }

    public void init(ShellDamengClient client, String schema) {
        this.clearItems();
        List<DamengSchema> schemas = client.schemas();
        if (CollectionUtil.isNotEmpty(schemas)) {
            this.setItem(schemas.stream().map(DamengSchema::getName).toList());
        }
        if (schema != null) {
            this.select(schema);
        }
    }
}
