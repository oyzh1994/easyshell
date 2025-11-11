package cn.oyzh.easyshell.fx.mysql;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db安全类型下拉框
 *
 * @author oyzh
 * @since 2024/08/07
 */
public class ShellMysqlSecurityTypeComboBox extends FXComboBox<String> {

    {
        this.addItem("DEFINER");
        this.addItem("INVOKER");
    }

    @Override
    public void select(String obj) {
        if (obj != null) {
            super.select(obj.toUpperCase());
        }
    }
}
