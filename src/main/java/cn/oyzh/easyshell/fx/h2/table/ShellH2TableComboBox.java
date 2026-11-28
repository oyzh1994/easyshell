package cn.oyzh.easyshell.fx.h2.table;

import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.ArrayList;
import java.util.List;

/**
 * H2 数据表下拉选择框。
 *
 * @since 2026-11-28
 */
public class ShellH2TableComboBox extends FXComboBox<String> {

    /**
     * 初始化数据表下拉选项。
     *
     * @param tableName 数据表名称
     * @param client    H2客户端
     */
    public void init(String tableName, ShellH2Client client) {
        List<String> tables = client == null ? List.of()
                : client.selectTables().parallelStream().map(H2Table::getName).toList();
        List<String> items = tables;
        if (tableName != null && tables.stream().noneMatch(item -> item.equalsIgnoreCase(tableName))) {
            items = new ArrayList<>(tables);
            items.add(tableName);
        }
        this.setItem(items);
        if (tableName != null) {
            this.select(tableName);
        } else {
            this.clearChild();
        }
    }
}
