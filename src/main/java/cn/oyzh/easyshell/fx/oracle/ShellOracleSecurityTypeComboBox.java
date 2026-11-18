package cn.oyzh.easyshell.fx.oracle;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * Oracle数据库安全类型下拉框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleSecurityTypeComboBox extends FXComboBox<String> {

    @Override
    public void select(String obj) {
        if (obj != null) {
            super.select(obj.toUpperCase());
        } else {
            super.clearSelection();
        }
    }

    @Override
    public void initNode() {
        this.addItem("");
        this.addItem("DEFINER");
        this.addItem("CURRENT_USER");
        super.initNode();
    }
}
