package cn.oyzh.easyshell.fx.mysql.routine;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * MySQL存储过程参数模式下拉框
 *
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
