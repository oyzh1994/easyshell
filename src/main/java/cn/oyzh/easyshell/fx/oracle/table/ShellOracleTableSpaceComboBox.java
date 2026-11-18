package cn.oyzh.easyshell.fx.oracle.table;

import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * Oracle表空间下拉选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleTableSpaceComboBox extends FXComboBox<String> {

    /**
     * 初始化表空间下拉选项
     *
     * @param client Oracle客户端
     */
    public void init(ShellOracleClient client) {
        this.clearItems();
        for (String tableSpace : client.tableSpaces()) {
            this.addItem(tableSpace);
        }
    }
}
