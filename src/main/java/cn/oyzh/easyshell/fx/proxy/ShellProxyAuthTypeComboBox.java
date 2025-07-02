package cn.oyzh.easyshell.fx.proxy;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025-04-18
 */
public class ShellProxyAuthTypeComboBox extends FXComboBox<String> {

    {
        this.addItem(I18nHelper.none());
        this.addItem(I18nHelper.password());
    }

    public boolean isPasswordAuth() {
        return this.getSelectedIndex() == 1;
    }

    public String getAuthType() {
        if (this.getSelectedIndex() == 0) {
            return "none";
        }
        return "password";
    }
}
