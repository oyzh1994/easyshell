package cn.oyzh.easyshell.fx.proxy;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2025-04-18
 */
public class ShellProxyProtocolComboBox extends FXComboBox<String> {

    {
        this.addItem("HTTP");
        this.addItem("SOCKS");
    }
}
