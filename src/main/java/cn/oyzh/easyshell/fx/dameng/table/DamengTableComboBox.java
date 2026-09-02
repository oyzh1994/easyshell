package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * db数据库选择框
 *
 * @author oyzh
 * @since 2024/01/25
 */
public class DamengTableComboBox extends FXComboBox<String> {

    public void init(String schema, ShellDamengClient client) {
        this.init(schema, null, client);
    }

    public void init(String schema, String tableName, ShellDamengClient client) {
        List<DamengTable> list = client.selectTables(schema);
        this.setItem(list.parallelStream().map(DamengTable::getName).toList());
        if (tableName != null) {
            this.select(tableName);
        }
    }
}
