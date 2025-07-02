package cn.oyzh.easyshell.fx;

import cn.oyzh.fx.gui.combobox.SSHAuthTypeCombobox;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025-04-03
 */
public class ShellAuthTypeComboBox extends SSHAuthTypeCombobox {

    {
        this.addItem(I18nHelper.key1Manager());
    }

    public boolean isManagerAuth() {
        return this.getSelectedIndex() == 3;
    }

    @Override
    public String getAuthType() {
        if (this.isManagerAuth()) {
            return "manager";
        }
        return super.getAuthType();
    }
}
