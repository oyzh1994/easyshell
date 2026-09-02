package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 引擎下拉选择框
 *
 * @author oyzh
 * @since 2024/01/26
 */
public class DamengTableSpaceComboBox extends FXComboBox<String> {

    public void init(ShellDamengClient client) {
        this.clearItems();
        for (String tableSpace : client.tableSpaces()) {
            this.addItem(tableSpace);
        }
    }
}
