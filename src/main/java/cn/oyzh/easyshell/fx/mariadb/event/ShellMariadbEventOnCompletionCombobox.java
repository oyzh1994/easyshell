package cn.oyzh.easyshell.fx.mariadb.event;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * MariaDB事件完成后保留策略下拉框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbEventOnCompletionCombobox extends FXComboBox<String> {

    {
        this.addItem("PRESERVE");
        this.addItem("NOT PRESERVE");
    }

    @Override
    public void select(String val) {
        if (val != null) {
            super.select(val.toUpperCase());
        } else {
            super.clearSelection();
        }
    }
}
