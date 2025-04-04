package cn.oyzh.easyshell.fx;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * shell终端类型选择框
 *
 * @author oyzh
 * @since 25/04/04
 */
public class ShellTermTypeComboBox extends FXComboBox<String> {

    {

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
