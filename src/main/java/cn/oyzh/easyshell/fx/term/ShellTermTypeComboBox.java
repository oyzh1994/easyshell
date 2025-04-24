package cn.oyzh.easyshell.fx.term;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * shell终端类型选择框
 *
 * @author oyzh
 * @since 25/04/04
 */
public class ShellTermTypeComboBox extends FXComboBox<String> {

    {
        this.addItem("");
        this.addItem("xterm-256color");
        this.addItem("xterm-color");
        this.addItem("xterm");
        this.addItem("linux");
        this.addItem("vt100");
        this.addItem("vt102");
        this.addItem("vt220");
        this.addItem("vt320");
        this.addItem("ansi");
        this.addItem("dump");
    }
}
