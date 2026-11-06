package cn.oyzh.easyshell.fx.postgresql.table;

import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * PostgreSQL表空间下拉选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlTableSpaceComboBox extends FXComboBox<String> {

    /**
     * 初始化表空间下拉选项
     *
     * @param client PostgreSQL客户端
     */
    public void init(ShellPostgresqlClient client) {
        this.clearItems();
        for (String tableSpace : client.tableSpaces()) {
            this.addItem(tableSpace);
        }
    }
}
