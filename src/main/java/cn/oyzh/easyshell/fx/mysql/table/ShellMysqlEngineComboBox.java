package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * MySQL存储引擎下拉框
 *
 * @author oyzh
 * @since 2024/01/26
 */
public class ShellMysqlEngineComboBox extends FXComboBox<String> {

    /**
     * 初始化存储引擎列表
     *
     * @param client MySQL客户端
     */
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
        } else {
            super.clearSelection();
        }
    }

    /**
     * 是否选中InnoDB引擎
     *
     * @return 是否选中InnoDB引擎
     */
    public boolean isInnoDB() {
        return "innoDB".equalsIgnoreCase(this.getSelectedItem());
    }
}
