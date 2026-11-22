package cn.oyzh.easyshell.fx.sqlserver.table;

import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * SqlServer表空间下拉选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerTableSpaceComboBox extends FXComboBox<String> {

    /**
     * 初始化表空间下拉选项
     *
     * @param client SqlServer客户端
     */
    public void init(ShellSqlServerClient client) {
        this.clearItems();
        for (String tableSpace : client.tableSpaces()) {
            this.addItem(tableSpace);
        }
    }
}
