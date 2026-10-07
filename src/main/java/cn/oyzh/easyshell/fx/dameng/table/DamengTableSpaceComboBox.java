package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 达梦表空间下拉选择框
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengTableSpaceComboBox extends FXComboBox<String> {

    /**
     * 初始化表空间下拉选项
     *
     * @param client 达梦客户端
     */
    public void init(ShellDamengClient client) {
        this.clearItems();
        for (String tableSpace : client.tableSpaces()) {
            this.addItem(tableSpace);
        }
    }
}
