package cn.oyzh.easyshell.fx.proxy;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025-04-18
 */
public class ShellProxyProtocolCombobox extends FXComboBox<String> {

    {
        this.addItem("HTTP");
        this.addItem("SOCKS4");
        this.addItem("SOCKS5");
    }
}
