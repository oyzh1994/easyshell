package cn.oyzh.easyshell.fx.ssh;

import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025-04-03
 */
public class ShellSSHAuthTypeComboBox2 extends ShellAuthTypeComboBox {

    {
        this.clearItems();
        this.addItem(I18nHelper.password());
        this.addItem(I18nHelper.certificate());
        this.addItem(I18nHelper.key1Manager());
    }

    @Override
    public boolean isManagerAuth() {
        return this.getSelectedIndex() == 2;
    }

    @Override
    public boolean isSSHAgentAuth() {
        return false;
    }
}
