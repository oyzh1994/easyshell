package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 引擎下拉选择框
 *
 * @author oyzh
 * @since 2024/01/26
 */
public class MysqlEngineComboBox extends FXComboBox<String> {

    public void init(ShellMysqlClient client) {
        this.clearItems();
        for (String engine : client.engines()) {
            this.addItem(engine.toUpperCase());
        }
    }

    @Override
    public void select(String engine) {
        if (engine != null) {
            super.select(engine.toUpperCase());
        }
    }

    public boolean isInnoDB() {
        return "innoDB".equalsIgnoreCase(this.getSelectedItem());
    }
}
