package cn.oyzh.easyshell.fx.tunneling;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025-04-16
 */
public class ShellTunnelingTypeComboBox extends FXComboBox<String> {

    {
        this.addItem(I18nHelper.local());
        this.addItem(I18nHelper.remote());
        this.addItem(I18nHelper.dynamic());
    }

    public boolean isLocalAuth() {
        return this.getSelectedIndex() == 0;
    }

    public boolean isRemoteAuth() {
        return this.getSelectedIndex() == 1;
    }

    public boolean isDynamicAuth() {
        return this.getSelectedIndex() == 2;
    }

    public String getTunnelingType() {
        if (this.isLocalAuth()) {
            return "local";
        }
        if (this.isRemoteAuth()) {
            return "remote";
        }
        return "dynamic";
    }

    public void setType(String type) {
        if ("local".equals(type)) {
            this.selectFirst();
        } else if ("remote".equals(type)) {
            this.select(1);
        } else if ("dynamic".equals(type)) {
            this.selectLast();
        }
    }
}
