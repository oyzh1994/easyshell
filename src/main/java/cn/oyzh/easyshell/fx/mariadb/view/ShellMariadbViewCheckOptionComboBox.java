package cn.oyzh.easyshell.fx.mariadb.view;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db视图检查选项下拉框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbViewCheckOptionComboBox extends FXComboBox<String> {

    {
        this.addItem("NONE");
        this.addItem("CASCADED");
        this.addItem("LOCAL");
    }

    @Override
    public void select(String obj) {
        if (obj != null) {
            super.select(obj.toUpperCase());
        } else {
            super.clearSelection();
        }
    }
}
