package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * 达梦数据表下拉选择框
 *
 * @author oyzh
 * @since 2024/01/25
 */
public class DamengTableComboBox extends FXComboBox<String> {

    /**
     * 初始化数据表下拉选项
     *
     * @param schema 模式名
     * @param client 达梦客户端
     */
    public void init(String schema, ShellDamengClient client) {
        this.init(schema, null, client);
    }

    /**
     * 初始化数据表下拉选项并选中指定数据表
     *
     * @param schema    模式名
     * @param tableName 数据表名
     * @param client    达梦客户端
     */
    public void init(String schema, String tableName, ShellDamengClient client) {
        List<DamengTable> list = client.selectTables(schema);
        this.setItem(list.parallelStream().map(DamengTable::getName).toList());
        if (tableName != null) {
            this.select(tableName);
        }
    }
}
