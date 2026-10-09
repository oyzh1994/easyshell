package cn.oyzh.easyshell.fx.mariadb.table;

import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * MariaDB存储引擎下拉框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbEngineComboBox extends FXComboBox<String> {

    /**
     * 初始化存储引擎列表
     *
     * @param client MariaDB客户端
     */
    public void init(ShellMariadbClient client) {
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
