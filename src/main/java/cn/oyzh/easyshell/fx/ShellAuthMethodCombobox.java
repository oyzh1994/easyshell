package cn.oyzh.easyshell.fx;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025-03-18
 */
public class ShellAuthMethodCombobox extends FXComboBox<String> {

    {
        this.addItem(I18nHelper.password());
        this.addItem(I18nHelper.certificate());
    }

    public boolean isPasswordAuth() {
        return this.getSelectedIndex() == 0;
    }

    public boolean isCertificateAuth() {
        return this.getSelectedIndex() == 1;
    }

    public String getAuthType() {
        return this.getSelectedIndex() == 0 ? "password" : "certificate";
    }
}
