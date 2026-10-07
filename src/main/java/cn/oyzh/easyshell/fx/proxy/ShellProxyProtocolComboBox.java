package cn.oyzh.easyshell.fx.proxy;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * 代理协议选择框
 *
 * @author oyzh
 * @since 2025-04-14
 */
public class ShellProxyProtocolComboBox extends FXComboBox<String> {

    {
        this.addItem("HTTP");
        this.addItem("SOCKS");
    }

    @Override
    public void select(String obj) {
        if (StringUtil.equalsAnyIgnoreCase(obj, "socks", "socks4", "socks5")) {
            this.select(1);
        } else {
            super.select(obj);
        }
    }
}
