package cn.oyzh.easyshell.fx.mysql.routine;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024/06/26
 */
public class ShellMysqlParamModeComboBox extends FXComboBox<String> {

    {
        this.addItem("IN");
        this.addItem("OUT");
        this.addItem("INOUT");
    }
}
