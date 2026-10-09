package cn.oyzh.easyshell.fx.mariadb.routine;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * MariaDB存储过程参数模式下拉框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbParamModeComboBox extends FXComboBox<String> {

    {
        this.addItem("IN");
        this.addItem("OUT");
        this.addItem("INOUT");
    }
}
